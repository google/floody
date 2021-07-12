/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.floody.spreadsheet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildAddRowsRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildDataValidationRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildDeleteSheetRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildNewSheetRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildProtectedRangeRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildStoreDataRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.setSpreadsheetTitleRequest;
import static com.google.floody.spreadsheet.SheetUtils.buildRangeA1NotationForRectangle;
import static com.google.floody.spreadsheet.SheetUtils.buildRangeA1NotationWithSheetName;
import static com.google.floody.spreadsheet.SheetUtils.isValidData;
import static com.google.floody.spreadsheet.SheetUtils.transformListToStringObjectList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchClearValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Response;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Concrete implementation of the Spreadsheet Service for Google Spreadsheets */
public class GoogleSpreadsheetService implements SpreadsheetService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  /** Credential for making Spreadsheet API calls. */
  private final Sheets sheetsService;
  /** GoogleSpreadsheetId to operate on after being initialized. */
  private final String spreadsheetId;
  /** Indicates which dimension an operation should apply to */
  private MajorDimension majorDimension;
  /** Determines how values should be rendered in the output. */
  private ValueRenderOption valueRenderOption;
  /** Determines how input data should be interpreted. */
  private ValueInputOption valueInputOption;

  /**
   * Instantiates the service with defaults for MajorDimension.ROW, ValueInputOption.RAW and
   * ValueRenderOption.UNFORMATTED_VALUE
   *
   * @param sheetsService initialized Google Sheets client*
   */
  public GoogleSpreadsheetService(Sheets sheetsService, String spreadsheetId) {
    checkArgument(!isBlank(spreadsheetId), "spreadsheet id can't be empty or null");

    this.sheetsService = sheetsService;
    this.majorDimension = MajorDimension.ROWS;
    this.valueRenderOption = ValueRenderOption.UNFORMATTED_VALUE;
    this.valueInputOption = ValueInputOption.RAW;
    this.spreadsheetId = spreadsheetId;
  }

  /** Returns the underlying Sheets service object to be used directly. */
  public Sheets getSheetsService() {
    return sheetsService;
  }

  @Override
  public String getSpreadsheetId() {
    return spreadsheetId;
  }

  @Override
  @Nullable
  public String getSpreadsheetUri() {
    try {
      return getSpreadsheet().getSpreadsheetUrl();
    } catch (IOException ioException) {
      return null;
    }
  }

  @Override
  public GoogleSpreadsheetMetaManager metaReader() throws IOException {
    return new GoogleSpreadsheetMetaManager(this);
  }

  @Override
  public GoogleSpreadsheetMetaManager metaWriter() {
    return new GoogleSpreadsheetMetaManager(this);
  }

  @Override
  public GoogleSpreadsheetService setMajorDimension(MajorDimension majorDimension) {
    checkNotNull(majorDimension, "majorDimension can't be null");

    this.majorDimension = majorDimension;
    return this;
  }

  @Override
  public GoogleSpreadsheetService setValueRenderOption(ValueRenderOption renderOption) {
    checkNotNull(renderOption, "Value Render Option can't be null");

    this.valueRenderOption = renderOption;
    return this;
  }

  @Override
  public GoogleSpreadsheetService setValueInputOption(ValueInputOption valueInputOption) {
    checkNotNull(valueInputOption, "inputOption can't be null");

    this.valueInputOption = valueInputOption;
    return this;
  }

  public Spreadsheet getSpreadsheet() throws IOException {
    return sheetsService.spreadsheets().get(spreadsheetId).execute();
  }

  @Override
  public int createNewSheet(String sheetName, @Nullable List<String> headers) throws IOException {
    int sheetId = createNewSheet(sheetName); // Insert New Sheet

    // Insert headers Data
    if (headers != null && !headers.isEmpty()) {
      storeData(sheetName, transformListToStringObjectList(headers), true);
    }

    return sheetId;
  }

  /**
   * Helper method for creating an empty Sheet inside the spreadsheet with the given name. If an
   * existing sheet with same name is found, it returns the existing sheetId.
   *
   * @param sheetName the name to be provided as the title of the sheet
   * @return the sheetId of the newly created sheet
   * @throws IOException when there is error in creating a new sheet
   */
  private int createNewSheet(String sheetName) throws IOException {

    Integer sheetId = getSheetIdForName(sheetName);
    if (isNull(sheetId)) {
      sheetId =
          sheetsService
              .spreadsheets()
              .batchUpdate(spreadsheetId, buildNewSheetRequest(sheetName))
              .execute()
              .getReplies()
              .get(0)
              .getAddSheet()
              .getProperties()
              .getSheetId();
    }

    return sheetId;
  }

  /**
   * Retrieves the sheet id in the initialized spreadsheet for the given sheet name or returns null.
   *
   * @param sheetName the name of the sheet to find in the spreadsheet.
   * @return the id of the sheet or null if not found.
   */
  private Integer getSheetIdForName(String sheetName) throws IOException {
    checkArgument(!isBlank(sheetName), "sheetName can't be empty or null");

    Optional<Integer> sheetId =
        sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
            .filter(sheet -> sheetName.equals(sheet.getProperties().getTitle()))
            .map(sheet -> sheet.getProperties().getSheetId())
            .findFirst();

    return sheetId.orElse(null);
  }

  @Override
  public void deleteSheet(Integer sheetId) {
    checkNotNull(sheetId, "null sheetId");

    try {
      sheetsService
          .spreadsheets()
          .batchUpdate(spreadsheetId, buildDeleteSheetRequest(sheetId))
          .execute();
    } catch (IOException ioexp) {
      logger.atSevere().withCause(ioexp).log(
          "SpreadsheetId: %s, SheetId: %d NOT Deleted", spreadsheetId, sheetId);
    }
  }

  @Override
  public void deleteSheet(String sheetName) {
    try {
      Integer sheetId = getSheetIdForName(sheetName);
      if (nonNull(sheetId)) {
        deleteSheet(sheetId);
      }
    } catch (IOException ioException) {

      logger.atSevere().withCause(ioException).log(
          "Exception in Retrieving SheetId of name: " + sheetName);
    }
  }

  @Override
  public void protectRange(
      Integer sheetId,
      String protectedRange,
      @Nullable String warningText,
      @Nullable List<String> groupEditors)
      throws IOException {

    sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetId,
            buildProtectedRangeRequest(
                sheetId,
                protectedRange,
                warningText,
                nonNull(groupEditors) ? ImmutableList.copyOf(groupEditors) : null))
        .execute();
  }

  @Override
  public void setDataValidationForValuesInList(
      Integer sheetId, Integer column, List<String> values, String helpText) throws IOException {

    sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetId,
            buildDataValidationRequest(sheetId, column, ImmutableList.copyOf(values), helpText))
        .execute();
  }

  @Override
  public <T> List<T> retrieveData(
      String sheetName, String range, SpreadsheetRowTransformer<T> transformer) throws IOException {

    checkArgument(!isBlank(sheetName), "sheetName can't be null or empty");
    checkNotNull(range, "Range can't be null");

    return loadGridData(sheetName, range).stream()
        .map(transformer::transformFromSheetRow)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private List<List<Object>> loadGridData(String sheetName, String range) throws IOException {
    return Optional.ofNullable(
            sheetsService
                .spreadsheets()
                .values()
                .get(spreadsheetId, buildRangeA1NotationWithSheetName(sheetName, range))
                .setMajorDimension("ROWS")
                .setValueRenderOption(valueRenderOption.toString())
                .execute()
                .getValues())
        .orElse(
            /* An Empty List representing an empty row. */
            ImmutableList.of());
  }

  @Override
  public <T> int storeDataWithTransform(
      String sheetName, SpreadsheetRowTransformer<T> transformer, Collection<T> data)
      throws IOException {
    List<List<Object>> transformedData =
        data.stream().map(transformer::transformToSheetRow).collect(toList());

    return storeData(sheetName, transformedData, false);
  }

  @Override
  public void clearData(String sheetName, String rangeA1Notation) throws IOException {

    sheetsService
        .spreadsheets()
        .values()
        .batchClear(
            spreadsheetId,
            new BatchClearValuesRequest()
                .setRanges(
                    ImmutableList.of(
                        buildRangeA1NotationWithSheetName(sheetName, rangeA1Notation))))
        .execute();
  }

  /**
   * Store the given data into the sheet by computing the base range and then writing to the API
   *
   * @param sheetName of the sheet to write the data
   * @param data the actual cell values in the sequence of MajorDimension
   * @param isFirstRow if the data to be updated contains the header or not
   * @return the number of Rows updated
   * @throws IOException when there is error in writing to the API
   */
  private int storeData(String sheetName, List<List<Object>> data, boolean isFirstRow)
      throws IOException {
    // Log an empty request instead of throwing exception
    if ((data == null) || data.isEmpty()) {
      logger.atInfo().log("Nothing to store in sheet (%s) - (data is empty) no rows", sheetName);
      return 0;
    }

    checkArgument(!data.get(0).isEmpty(), "(data is empty) row contains no cells");
    checkArgument(isValidData(data), "Invalid data, empty or null values");

    String startingCell = "A" + (isFirstRow ? 1 : 2);
    String baseRange = buildRangeA1NotationForRectangle(startingCell, data.get(0).size(), null);
    clearData(sheetName, baseRange);

    // Write data to the spreadsheet
    return storeData(sheetName, startingCell, data);
  }

  @Override
  public int storeData(String sheetName, String startCell, List<List<Object>> data)
      throws IOException {
    return sheetsService
        .spreadsheets()
        .values()
        .batchUpdate(
            spreadsheetId,
            buildStoreDataRequest(
                sheetName,
                data.stream().map(ImmutableList::copyOf).collect(toImmutableList()),
                startCell,
                majorDimension,
                valueInputOption,
                valueRenderOption))
        .execute()
        .getTotalUpdatedRows();
  }

  @Override
  public void addMoreRows(String sheetName, int numRows) throws IOException {
    Optional<Sheet> sheetToUpdate =
        sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
            .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
            .findFirst();

    if (sheetToUpdate.isEmpty()) {
      throw new IllegalArgumentException(String.format("Sheet missing: %s", sheetName));
    }

    int lastRowIndex = sheetToUpdate.get().getProperties().getGridProperties().getRowCount();
    int sheetId = sheetToUpdate.get().getProperties().getSheetId();

    sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetId,
            new BatchUpdateSpreadsheetRequest()
                .setRequests(ImmutableList.of(buildAddRowsRequest(sheetId, lastRowIndex, numRows))))
        .execute();
  }

  @Override
  public String getSpreadsheetTitle() {
    try {
      return sheetsService.spreadsheets().get(spreadsheetId).execute().getProperties().getTitle();
    } catch (IOException ioexp) {
      return StringUtils.EMPTY;
    }
  }

  @Override
  public void setSpreadsheetTitle(String newTitle) throws IOException {
    sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetId,
            new BatchUpdateSpreadsheetRequest()
                .setRequests(ImmutableList.of(setSpreadsheetTitleRequest(newTitle))))
        .execute();
  }

  @Override
  public ImmutableMap<Integer, Integer> lockSheets(String... lockSheetNames) throws IOException {
    ImmutableSet<String> sheetNames = ImmutableSet.copyOf(lockSheetNames);

    List<Sheet> sheets = sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets();

    List<Request> sheetProtectRequests =
        sheets.stream()
            .filter(sheet -> sheetNames.contains(sheet.getProperties().getTitle()))
            .map(sheet -> sheet.getProperties().getSheetId())
            .map(GoogleSpreadsheetRequestBuilder::buildProtectSheetRequest)
            .collect(toImmutableList());

    ImmutableMap<Integer, Integer> lockedSheets =
        sheetsService
            .spreadsheets()
            .batchUpdate(
                spreadsheetId,
                new BatchUpdateSpreadsheetRequest().setRequests(sheetProtectRequests))
            .execute()
            .getReplies()
            .stream()
            .map(Response::getAddProtectedRange)
            .collect(
                collectingAndThen(
                    toMap(
                        addResponse -> addResponse.getProtectedRange().getRange().getSheetId(),
                        addResponse -> addResponse.getProtectedRange().getProtectedRangeId()),
                    ImmutableMap::copyOf));

    logger.atInfo().log("lockedSheets -> %s", lockedSheets);

    return lockedSheets;
  }

  @Override
  public void unlockSheet(Integer... protectedRangeIds) throws IOException {
    unlockSheets(ImmutableSet.copyOf(protectedRangeIds));
  }

  @Override
  public void unlockSheets(Collection<Integer> protectedRangeIds) throws IOException {
    ImmutableList<Request> deleteProtectedRangeRequests =
        protectedRangeIds.stream()
            .distinct()
            .map(GoogleSpreadsheetRequestBuilder::buildDeleteProtectedRangeRequest)
            .collect(toImmutableList());

    sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetId,
            new BatchUpdateSpreadsheetRequest().setRequests(deleteProtectedRangeRequests))
        .execute();
  }

  @Override
  public void unlockSheets(String... unlockSheetNames) throws IOException {
    ImmutableSet<String> sheetNames = ImmutableSet.copyOf(unlockSheetNames);

    ImmutableSet<Integer> protectedSheetId =
        sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
            .filter(sheet -> sheetNames.contains(sheet.getProperties().getTitle()))
            .flatMap(sheet -> sheet.getProtectedRanges().stream())
            .filter(GoogleSpreadsheetService::isEntireSheetProtected)
            .map(ProtectedRange::getProtectedRangeId)
            .collect(toImmutableSet());

    unlockSheets(protectedSheetId);

    logger.atInfo().log("unlocked sheets");
  }

  private static boolean isEntireSheetProtected(ProtectedRange protectedRange) {
    GridRange range = protectedRange.getRange();
    return (range.getSheetId() != null)
        && (range.getStartColumnIndex() == null)
        && (range.getEndColumnIndex() == null)
        && (range.getStartRowIndex() == null)
        && (range.getEndRowIndex() == null);
  }
}

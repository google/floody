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
import static com.google.floody.spreadsheet.SheetUtils.buildGridRangeFromA1Notation;
import static com.google.floody.spreadsheet.SheetUtils.buildRangeA1NotationForRectangle;
import static com.google.floody.spreadsheet.SheetUtils.buildRangeA1NotationWithSheetName;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BooleanCondition;
import com.google.api.services.sheets.v4.model.ConditionValue;
import com.google.api.services.sheets.v4.model.CreateDeveloperMetadataRequest;
import com.google.api.services.sheets.v4.model.DataFilter;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.DeleteProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.DeveloperMetadata;
import com.google.api.services.sheets.v4.model.DeveloperMetadataLocation;
import com.google.api.services.sheets.v4.model.DeveloperMetadataLookup;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Editors;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.InsertDimensionRequest;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SearchDeveloperMetadataRequest;
import com.google.api.services.sheets.v4.model.SetDataValidationRequest;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Static utility methods to build {@link Sheets} request objects. */
public final class GoogleSpreadsheetRequestBuilder {

  private GoogleSpreadsheetRequestBuilder() {}

  /**
   * Builds the API request for protecting a Sheet Range
   *
   * @param sheetId sheet id of the Sheet containing the protected range
   * @param protectedRangeA1Notation A1 notation of the range to protect e.g. A1:B2 or A:B
   * @param helpText the error message to show users when they try to edit a protected range
   * @param groupEditors google group email addresses of the editors for protected range
   * @return the ProtectedRange Request with right parameters set for protecting a GoogleSpreadsheet
   *     Range
   */
  public static BatchUpdateSpreadsheetRequest buildProtectedRangeRequest(
      Integer sheetId,
      String protectedRangeA1Notation,
      @Nullable String helpText,
      @Nullable ImmutableList<String> groupEditors) {

    checkArgument(nonNull(sheetId) && sheetId >= 0, "sheetId can't be null or negative");
    checkArgument(nonNull(protectedRangeA1Notation), "protected Range can't be null");

    ProtectedRange protectedRange =
        new ProtectedRange()
            .setRange(buildGridRangeFromA1Notation(protectedRangeA1Notation).setSheetId(sheetId))
            .setDescription(Optional.ofNullable(helpText).orElse("System Generated Values"))
            .setRequestingUserCanEdit(false)
            .setWarningOnly(false);

    if (nonNull(groupEditors) && !groupEditors.isEmpty()) {
      protectedRange.setEditors(new Editors().setGroups(groupEditors));
    }

    return new BatchUpdateSpreadsheetRequest()
        .setRequests(
            ImmutableList.of(
                new Request()
                    .setAddProtectedRange(
                        new AddProtectedRangeRequest().setProtectedRange(protectedRange))));
  }

  /**
   * Builds the Add New Sheet API request
   *
   * @param sheetName the title of the new sheet to be created
   * @return the completed AddSheetRequest with right SheetName as title
   */
  public static BatchUpdateSpreadsheetRequest buildNewSheetRequest(String sheetName) {

    checkArgument(nonNull(sheetName) && !isBlank(sheetName), "sheetName is empty or null");

    return new BatchUpdateSpreadsheetRequest()
        .setRequests(
            ImmutableList.of(
                new Request()
                    .setAddSheet(
                        new AddSheetRequest()
                            .setProperties(new SheetProperties().setTitle(sheetName)))));
  }

  /**
   * Helper method to build a Data Validation Request with a default to show a drop-down UI of
   * possible choices for one single column validation
   *
   * @param sheetId of the sheet which contains the range
   * @param column the column number of the column to contain the validation (0-indexed)
   * @param values the possible/valid values in the column
   * @param helpText to show users when they enter an invalid value in the column
   * @return A complete DataValidation Request with list validation and strict validation
   */
  public static BatchUpdateSpreadsheetRequest buildDataValidationRequest(
      Integer sheetId, Integer column, ImmutableList<String> values, String helpText) {
    checkArgument(nonNull(sheetId) && sheetId >= 0, "sheetId can't be null or negative");
    checkArgument(nonNull(column) && column >= 0, "column can't be null or negative");
    checkArgument(
        nonNull(values) && !values.isEmpty(),
        "pre-defined data validation values can't be null or empty");
    checkNotNull(helpText, "helpText to show warning to users' shouldn't be null");

    return new BatchUpdateSpreadsheetRequest()
        .setRequests(
            ImmutableList.of(
                new Request()
                    .setSetDataValidation(
                        new SetDataValidationRequest()
                            .setRange(
                                new GridRange()
                                    .setSheetId(sheetId)
                                    .setStartColumnIndex(column)
                                    .setEndColumnIndex(column + 1)
                                    .setStartRowIndex(1))
                            .setRule(
                                new DataValidationRule()
                                    .setInputMessage(helpText)
                                    .setShowCustomUi(true)
                                    .setStrict(true)
                                    .setCondition(
                                        new BooleanCondition()
                                            .setType("ONE_OF_LIST")
                                            .setValues(
                                                values.stream()
                                                    .map(
                                                        item ->
                                                            new ConditionValue()
                                                                .setUserEnteredValue(item))
                                                    .collect(Collectors.toList())))))));
  }

  /**
   * Build a BatchSpreadsheet update request with data to be updated in the spreadsheet the sheet
   * range is automatically chosen from 2nd Row/Column based on MajorDimension
   *
   * @param sheetName of the sheet to be updated
   * @param data the Array of data to be updated
   * @param startingCell the starting range of the data to be updated
   * @return the filled sheet Data update Request
   */
  public static BatchUpdateValuesRequest buildStoreDataRequest(
      String sheetName,
      ImmutableList<List<Object>> data,
      String startingCell,
      @Nullable MajorDimension majorDimension,
      @Nullable ValueInputOption valueInputOption,
      @Nullable ValueRenderOption valueRenderOption) {
    checkArgument(nonNull(sheetName) && !sheetName.isEmpty(), "sheetName can't be null ");
    checkArgument(
        SheetUtils.isValidData(data), "Data needs to be valid and contain at least 1 element");

    String range = buildRangeA1NotationForRectangle(startingCell, data.get(0).size(), data.size());

    return new BatchUpdateValuesRequest()
        .setValueInputOption(
            Optional.ofNullable(valueInputOption).orElse(ValueInputOption.USER_ENTERED).toString())
        .setResponseValueRenderOption(
            Optional.ofNullable(valueRenderOption)
                .orElse(ValueRenderOption.UNFORMATTED_VALUE)
                .toString())
        .setIncludeValuesInResponse(false)
        .setData(
            ImmutableList.of(
                new ValueRange()
                    .setRange(buildRangeA1NotationWithSheetName(sheetName, range))
                    .setMajorDimension(majorDimension.toString())
                    .setValues(data)));
  }

  /**
   * Build a Google Spreadsheet Delete Sheet Request
   *
   * @param sheetId of the sheet to be deleted
   * @return the request object to delete a sheet
   */
  public static BatchUpdateSpreadsheetRequest buildDeleteSheetRequest(Integer sheetId) {
    checkArgument(nonNull(sheetId) && sheetId >= 0, "Invalid SheetId, should be greater than zero");

    return new BatchUpdateSpreadsheetRequest()
        .setIncludeSpreadsheetInResponse(false)
        .setResponseIncludeGridData(false)
        .setRequests(
            ImmutableList.of(
                new Request().setDeleteSheet(new DeleteSheetRequest().setSheetId(sheetId))));
  }

  /**
   * Build a Request to insert a new DeveloperMetadata information in the spreadsheet.
   *
   * @param metaDataValues a list of key-value pairs to add in the spreadsheet.
   * @return a Request object to add DeveloperMetadata.
   */
  public static BatchUpdateSpreadsheetRequest buildAddDeveloperMetadataRequest(
      ImmutableMap<String, String> metaDataValues) {
    checkArgument(nonNull(metaDataValues) && !metaDataValues.isEmpty());

    return new BatchUpdateSpreadsheetRequest()
        .setRequests(
            metaDataValues.entrySet().stream()
                .map(
                    pair ->
                        new Request()
                            .setCreateDeveloperMetadata(
                                new CreateDeveloperMetadataRequest()
                                    .setDeveloperMetadata(
                                        new DeveloperMetadata()
                                            .setLocation(
                                                new DeveloperMetadataLocation()
                                                    .setSpreadsheet(true))
                                            .setVisibility("DOCUMENT")
                                            .setMetadataKey(pair.getKey())
                                            .setMetadataValue(pair.getValue()))))
                .collect(toList()));
  }

  /**
   * Build a Request to search DeveloperMetadata stored in the spreadsheet, for the given keys.
   *
   * @param keys a list of keys to search in the spreadsheet's metadata.
   * @return a Request object to search DeveloperMetadata for given keys.
   */
  public static SearchDeveloperMetadataRequest buildSearchMetadataRequest(String[] keys) {
    checkArgument(keys != null && keys.length > 0);

    return new SearchDeveloperMetadataRequest()
        .setDataFilters(
            Arrays.stream(keys)
                .map(
                    key ->
                        new DataFilter()
                            .setDeveloperMetadataLookup(
                                new DeveloperMetadataLookup().setMetadataKey(key)))
                .collect(toList()));
  }

  public static Request buildAddRowsRequest(int sheetId, int lastRowIndex, int numRows) {
    return new Request()
        .setInsertDimension(
            new InsertDimensionRequest()
                .setInheritFromBefore(true)
                .setRange(
                    new DimensionRange()
                        .setDimension("ROWS")
                        .setSheetId(sheetId)
                        .setStartIndex(lastRowIndex)
                        .setEndIndex(lastRowIndex + numRows)));
  }

  public static Request setSpreadsheetTitleRequest(String newTitle) {
    return new Request()
        .setUpdateSpreadsheetProperties(
            new UpdateSpreadsheetPropertiesRequest()
                .setProperties(new SpreadsheetProperties().setTitle(newTitle)));
  }

  public static Request buildProtectSheetRequest(int sheetId) {
    return new Request()
        .setAddProtectedRange(
            new AddProtectedRangeRequest()
                .setProtectedRange(
                    new ProtectedRange()
                        .setRange(new GridRange().setSheetId(sheetId))
                        .setWarningOnly(false)
                        .setRequestingUserCanEdit(false)
                        .setDescription("Sheet is under processing")));
  }

  public static Request buildDeleteProtectedRangeRequest(int protectedRangeId) {
    return new Request()
        .setDeleteProtectedRange(
            new DeleteProtectedRangeRequest().setProtectedRangeId(protectedRangeId));
  }
}

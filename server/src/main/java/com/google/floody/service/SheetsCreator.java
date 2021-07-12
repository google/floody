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

package com.google.floody.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.floody.model.ActivitySheetHeaderInformation.FLOODLIGHT_STATUS_COLUMN;
import static com.google.floody.model.ActivitySheetHeaderInformation.REMARKS_COLUMN;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildDataValidationRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildProtectedRangeRequest;
import static java.lang.Integer.parseInt;

import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Editors;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.floody.model.ActivityGroupSheetHeaderInformation;
import com.google.floody.model.ActivitySheetHeaderInformation;
import com.google.floody.model.CacheBustingType;
import com.google.floody.model.CustomVariablesSheetHeaderInformation;
import com.google.floody.model.DefaultTagSheetHeaderInformation;
import com.google.floody.model.FloodlightActivityStatus;
import com.google.floody.model.FloodyCountingMethod;
import com.google.floody.model.PublisherTagConversionType;
import com.google.floody.model.PublisherTagSheetHeaderInformation;
import com.google.floody.model.TagFormat;
import com.google.floody.model.TagType;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

@AutoValue
abstract class SheetsCreator {

  abstract GoogleSpreadsheetService spreadsheetService();

  abstract ImmutableList<String> floodyAdminEmails();

  public static SheetsCreator create(
      GoogleSpreadsheetService spreadsheetService, ImmutableList<String> floodyAdminEmails) {
    return new AutoValue_SheetsCreator(spreadsheetService, floodyAdminEmails);
  }

  public void createAllSheets() throws IOException {
    var activityActions = createActivitySheet();
    var defaultTagSheetActions = createDefaultTagSheet();
    var publisherTagSheetActions = createPublisherTagSheet();
    var userDefinedVariablesSheetActions = createUserDefinedVariablesSheet();
    var activityGroupSheetActions = createActivityGroupSheet();

    var allBatchRequests =
        ImmutableList.<Request>builder()
            .addAll(activityActions)
            .addAll(defaultTagSheetActions)
            .addAll(publisherTagSheetActions)
            .addAll(userDefinedVariablesSheetActions)
            .addAll(activityGroupSheetActions)
            .build();

    spreadsheetService()
        .getSheetsService()
        .spreadsheets()
        .batchUpdate(
            spreadsheetService().getSpreadsheetId(),
            new BatchUpdateSpreadsheetRequest().setRequests(allBatchRequests))
        .execute();
  }

  /**
   * Returns an RGB Color object for a given hexColorCode
   *
   * @param hexColorCode hexCode color code (e.g. #ccbbaa)
   */
  private Color buildColorFromHexCode(String hexColorCode) {
    return new Color()
        .setRed((float) parseInt(hexColorCode.substring(1, 3), 16) / 256)
        .setGreen((float) parseInt(hexColorCode.substring(3, 5), 16) / 256)
        .setBlue((float) parseInt(hexColorCode.substring(5, 7), 16) / 256);
  }

  /**
   * Returns Requests to format headers of a sheet to have a background color and text warping.
   *
   * @param sheetId the id of the sheet to format.
   */
  private ImmutableList<Request> buildHeaderRowFormatRequest(int sheetId) {
    return ImmutableList.of(
        new Request()
            .setRepeatCell(
                new RepeatCellRequest()
                    .setRange(
                        new GridRange().setSheetId(sheetId).setStartRowIndex(0).setEndRowIndex(1))
                    .setCell(
                        new CellData()
                            .setUserEnteredFormat(
                                new CellFormat()
                                    .setWrapStrategy("WRAP")
                                    .setTextFormat(new TextFormat().setBold(true))
                                    .setBackgroundColor(buildColorFromHexCode("#cccccc"))))
                    .setFields("userEnteredFormat(wrapStrategy,textFormat,backgroundColor)")),
        new Request()
            .setUpdateSheetProperties(
                new UpdateSheetPropertiesRequest()
                    .setProperties(
                        new SheetProperties()
                            .setSheetId(sheetId)
                            .setGridProperties(new GridProperties().setFrozenRowCount(1)))
                    .setFields("gridProperties.frozenRowCount")));
  }

  /** Creates a new sheet for Floodlight Activities with all validations and headers. */
  private ImmutableList<Request> createActivitySheet() throws IOException {
    int activitySheetId =
        spreadsheetService()
            .createNewSheet(
                ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME,
                ActivitySheetHeaderInformation.ACTIVITY_SHEET_HEADERS);
    ImmutableList.Builder<Request> batchUpdateRequestBuilder = ImmutableList.builder();

    batchUpdateRequestBuilder
        // Free First Row
        .add(
            new Request()
                .setUpdateSheetProperties(
                    new UpdateSheetPropertiesRequest()
                        .setProperties(
                            new SheetProperties()
                                .setSheetId(activitySheetId)
                                .setGridProperties(new GridProperties().setFrozenRowCount(1)))
                        .setFields("gridProperties.frozenRowCount")))
        .addAll(
            buildProtectedRangeRequest(
                    activitySheetId,
                    "A:C",
                    "System information columns are protected",
                    floodyAdminEmails())
                .getRequests())
        .addAll(
            buildProtectedRangeRequest(
                    activitySheetId, "T:T", "System Remarks column", floodyAdminEmails())
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.FLAG_TO_UPDATE_COLUMN,
                    ImmutableList.of("Y", StringUtils.EMPTY),
                    "Choose 'Y' to update rows.")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.FLOODLIGHT_STATUS_COLUMN,
                    ImmutableList.of(
                        FloodlightActivityStatus.ACTIVE.name(),
                        FloodlightActivityStatus.ARCHIVED_AND_DISABLED.name()),
                    "Choose 'ARCHIVED_AND_DISABLED' to archive Activity")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.CREATE_AUDIENCE_COLUMN,
                    ImmutableList.of("Y", StringUtils.EMPTY),
                    "Choose 'Y' to auto create audience")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.COUNTING_METHODOLOGY_COLUMN,
                    buildEnumValueNameList(FloodyCountingMethod.values()),
                    "pre-defined counting methods")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.CACHE_BUSTING_COLUMN,
                    buildEnumValueNameList(CacheBustingType.values()),
                    "pre-defined cache-busting types")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.TAG_FORMAT_COLUMN,
                    buildEnumValueNameList(TagFormat.values()),
                    "choose from pre-defined counting methods")
                .getRequests())
        .addAll(
            buildDataValidationRequest(
                    activitySheetId,
                    ActivitySheetHeaderInformation.TAG_TYPE_COLUMN,
                    buildEnumValueNameList(TagType.values()),
                    "choose from pre-defined tag types.")
                .getRequests())
        // Header colors
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setWrapStrategy("WRAP")
                                        .setTextFormat(new TextFormat().setBold(true))))
                        .setFields("userEnteredFormat(wrapStrategy,textFormat)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(0)
                                .setEndColumnIndex(2))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light red*/ "#ea9999"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(FLOODLIGHT_STATUS_COLUMN)
                                .setEndColumnIndex(REMARKS_COLUMN))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light red*/ "#ea9999"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(2)
                                .setEndColumnIndex(6))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light orange*/ "#fff2cc"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(3)
                                .setEndColumnIndex(4))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*yellow*/ "#fff200"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(6)
                                .setEndColumnIndex(8))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light green*/ "#d9ead3"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(8)
                                .setEndColumnIndex(11))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light violet*/ "#b4a7d6"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(11)
                                .setEndColumnIndex(14))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*skyblue*/ "#cfe2f3"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(14)
                                .setEndColumnIndex(16))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*grey*/ "#cccccc"))))
                        .setFields("userEnteredFormat(backgroundColor)")))
        .add( // Resize the columns to auto-fit contents
            new Request()
                .setAutoResizeDimensions(
                    new AutoResizeDimensionsRequest()
                        .setDimensions(
                            new DimensionRange()
                                .setDimension("COLUMNS")
                                .setSheetId(activitySheetId)
                                .setStartIndex(ActivitySheetHeaderInformation.ACCOUNT_ID_COLUMN)
                                .setEndIndex(ActivitySheetHeaderInformation.REMARKS_COLUMN))))
        .add(
            new Request()
                .setRepeatCell(
                    new RepeatCellRequest()
                        .setRange(
                            new GridRange()
                                .setSheetId(activitySheetId)
                                .setStartRowIndex(0)
                                .setEndRowIndex(1)
                                .setStartColumnIndex(
                                    ActivitySheetHeaderInformation.CREATE_AUDIENCE_COLUMN)
                                .setEndColumnIndex(ActivitySheetHeaderInformation.REMARKS_COLUMN))
                        .setCell(
                            new CellData()
                                .setUserEnteredFormat(
                                    new CellFormat()
                                        .setBackgroundColor(
                                            buildColorFromHexCode(/*light red*/ "#ea9999"))))
                        .setFields("userEnteredFormat(backgroundColor)")));

    return batchUpdateRequestBuilder.build();
  }

  /** Creates a new sheet for Default Tags with correct headers. */
  private ImmutableList<Request> createDefaultTagSheet() throws IOException {
    int defaultTagSheetId =
        spreadsheetService()
            .createNewSheet(
                DefaultTagSheetHeaderInformation.DEFAULT_TAG_SHEET_NAME,
                DefaultTagSheetHeaderInformation.DEFAULT_TAG_SHEET_HEADERS);

    return ImmutableList.copyOf(buildHeaderRowFormatRequest(defaultTagSheetId));
  }

  /** Creates a new sheet for Publisher Tags with correct headers. */
  private ImmutableList<Request> createPublisherTagSheet() throws IOException {
    int publisherTagSheetId =
        spreadsheetService()
            .createNewSheet(
                PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_NAME,
                PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_HEADERS);

    return ImmutableList.<Request>builder()
        .addAll(
            buildDataValidationRequest(
                    publisherTagSheetId,
                    PublisherTagSheetHeaderInformation.PUBLISHER_TAG_CONVERSION_TYPE_COLUMN,
                    buildEnumValueNameList(PublisherTagConversionType.values()),
                    "choose from pre-defined conversion types")
                .getRequests())
        .addAll(buildHeaderRowFormatRequest(publisherTagSheetId))
        .build();
  }

  /** Creates a new sheet for Custom Variables/ User Defined Variables with correct headers. */
  private ImmutableList<Request> createUserDefinedVariablesSheet() throws IOException {
    int sheetId =
        spreadsheetService()
            .createNewSheet(
                CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_SHEET_NAME,
                CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_SHEET_HEADERS);

    return ImmutableList.<Request>builder()
        .add(
            new Request()
                .setAddProtectedRange(
                    new AddProtectedRangeRequest()
                        .setProtectedRange(
                            new ProtectedRange()
                                .setEditors(new Editors().setGroups(floodyAdminEmails()))
                                .setRange(new GridRange().setSheetId(sheetId))
                                .setDescription("Please edit custom variable using DCM UI"))))
        .addAll(buildHeaderRowFormatRequest(sheetId))
        .build();
  }

  /** Creates a new (user read-only) sheet for Floodlight Activity Groups with correct headers. */
  private ImmutableList<Request> createActivityGroupSheet() throws IOException {
    int activityGroupSheetId =
        spreadsheetService()
            .createNewSheet(
                ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_SHEET_NAME,
                ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_SHEET_HEADERS);

    return ImmutableList.<Request>builder()
        .addAll(buildHeaderRowFormatRequest(activityGroupSheetId))
        .add(
            new Request()
                .setAddProtectedRange(
                    new AddProtectedRangeRequest()
                        .setProtectedRange(
                            new ProtectedRange()
                                .setRange(new GridRange().setSheetId(activityGroupSheetId))
                                .setEditors(new Editors().setGroups(floodyAdminEmails())))))
        .build();
  }

  private ImmutableList<String> buildEnumValueNameList(Enum<?>[] values) {
    return Arrays.stream(values).map(Enum::name).collect(toImmutableList());
  }
}

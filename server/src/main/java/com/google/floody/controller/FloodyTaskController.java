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

package com.google.floody.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.floody.model.ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME;
import static com.google.floody.model.DefaultTagSheetHeaderInformation.DEFAULT_TAG_SHEET_NAME;
import static com.google.floody.model.PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.flogger.GoogleLogger;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/floody")
public class FloodyTaskController extends FloodyBaseController {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  /**
   * Reads the DCM Floodlight Activities from the Floodlight configuration and writes to the
   * provided spreadsheet Id
   *
   * @param profileId the User's DCM profile Id to be used for the operation.
   * @param spreadsheetId the spreadsheet id to update
   * @throws IOException in case of errors in DCM API or Sheets API
   */
  @GetMapping("/exportToSheet/{spreadsheetId}")
  public void exportToSheet(
      @RequestHeader(value = "profile") Long profileId, @PathVariable String spreadsheetId)
      throws IOException {
    checkArgument(!isBlank(spreadsheetId), "spreadsheetId should not be null or empty");
    checkArgument((profileId != null) && profileId > 0, "Invalid profileId (%s)", profileId);

    logger.atInfo().log("profileId (%s)", profileId);

    var spreadsheetService = robotServicesFactory().buildSpreadsheetService(spreadsheetId);

    // Lock the spreadsheet
    var lockedSheetRanges =
        spreadsheetService.lockSheets(
            ACTIVITY_SHEET_NAME, DEFAULT_TAG_SHEET_NAME, PUBLISHER_TAG_SHEET_NAME);

    try {
      robotServicesFactory()
          .buildFloodyService()
          .readFromDcm(userServicesFactory().buildDcmServiceFactory())
          .forSpreadsheet(spreadsheetId)
          .loadFor(profileId)
          .toSheets(robotServicesFactory().buildSpreadsheetService(spreadsheetId))
          .sync();
    } finally {
      // unlock the spreadsheet after processing or in case an Exception is thrown.

      if (lockedSheetRanges != null) {
        spreadsheetService.unlockSheets(lockedSheetRanges.values());
      } else {
        spreadsheetService.unlockSheets(
            ACTIVITY_SHEET_NAME, DEFAULT_TAG_SHEET_NAME, PUBLISHER_TAG_SHEET_NAME);
      }
    }
  }

  /**
   * Reads from the provided spreadsheet id and updates the DCM Floodlight Configuration.
   *
   * @param profileId the User's DCM profile Id to be used for the operation.
   * @param spreadsheetId the spreadsheet id to update from.
   * @throws IOException in case of errors in DCM API or Sheets API
   */
  @GetMapping("/exportToDcm/{spreadsheetId}")
  public void exportToDcm(
      @RequestHeader(value = "profile") Long profileId, @PathVariable String spreadsheetId)
      throws IOException {
    checkArgument((profileId != null) && profileId > 0, "Invalid profileId");
    logger.atInfo().log("profileId (%s)", profileId);

    var spreadsheetService = robotServicesFactory().buildSpreadsheetService(spreadsheetId);

    // Lock the spreadsheet
    var lockedSheetRanges =
        spreadsheetService.lockSheets(
            ACTIVITY_SHEET_NAME, DEFAULT_TAG_SHEET_NAME, PUBLISHER_TAG_SHEET_NAME);

    try {
      robotServicesFactory()
          .buildFloodyService()
          .readFromSheets()
          .forSpreadsheet(spreadsheetId)
          .load()
          .toDcm(userServicesFactory().buildDcmService())
          .buildDcmWriter(profileId)
          .sync()
          .toSheets(spreadsheetService)
          .sync();
    } finally {
      // Unlock the spreadsheet after processing
      if (lockedSheetRanges != null) {
        spreadsheetService.unlockSheets(lockedSheetRanges.values());
      } else {
        spreadsheetService.unlockSheets(
            ACTIVITY_SHEET_NAME, DEFAULT_TAG_SHEET_NAME, PUBLISHER_TAG_SHEET_NAME);
      }
    }
  }
}

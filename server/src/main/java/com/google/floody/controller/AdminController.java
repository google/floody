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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.common.hash.Hashing;
import com.google.floody.model.ActivitySheetHeaderInformation;
import com.google.floody.model.FloodyProperties;
import com.google.floody.protobuf.AdminOperations.ClientInformation;
import com.google.floody.protobuf.FileOperations.ShareSpreadsheetRequest;
import com.google.floody.protobuf.SheetObjects.FloodySheet;
import com.google.floody.spreadsheet.SharingService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public final class AdminController extends FloodyBaseController {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Autowired private FloodyProperties floodyProperties;

  @GetMapping("/clientId")
  public ClientInformation retrieveClientId() {
    return ClientInformation.newBuilder().setClientId(floodyProperties.getClientId()).build();
  }

  @GetMapping("/init/{accountId:[\\d]+}/{floodlightConfigurationId:[\\d]+}")
  public FloodySheet initializeNewSpreadsheet(
      @PathVariable long accountId, @PathVariable long floodlightConfigurationId)
      throws IOException {

    var userEmail = userServicesFactory().getAccountEmail();
    logger.atInfo().log("userHashedEmail %s", Hashing.sha256().hashString(userEmail, UTF_8));

    return robotServicesFactory()
        .buildSpreadsheetCreator()
        .createSpreadsheet(accountId, floodlightConfigurationId, userEmail);
  }

  /**
   * Adds 100 rows at the bottom of the spreadsheet
   *
   * @param spreadsheetId the Floody spreadsheet to add rows to
   * @param numberOfRows the number of rows to add to the spreadsheet
   * @throws IOException when SPreadsheet API errors out.
   */
  @GetMapping("/addRows/{spreadsheetId}")
  public void addMoreRows(
      HttpServletResponse httpServletResponse,
      @PathVariable String spreadsheetId,
      @RequestHeader(
              required = false,
              defaultValue = ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME)
          String sheetName,
      @RequestHeader(required = false, defaultValue = "100") int numberOfRows)
      throws IOException {

    logger.atInfo().log("sheetName: %s | numberOfRows: %d", sheetName, numberOfRows);

    verifyUserHasFullAuthOrReturn(spreadsheetId);

    robotServicesFactory()
        .buildSpreadsheetService(spreadsheetId)
        .addMoreRows(sheetName, numberOfRows);
  }

  /**
   * Updates the sheet's title using provided user credentails to ensure that the user has edit
   * rights on the sheet.
   *
   * @param spreadsheetId the id of the spreadsheet for which to update title.
   * @param newTitle the updated title to use for the given spreadsheet.
   * @return true if the change is successful, false otherwise.
   */
  @GetMapping(value = "/updateTitle/{spreadsheetId}/{newTitle}")
  public boolean updateSpreadsheetTitle(
      @PathVariable String spreadsheetId, @PathVariable String newTitle) {

    logger.atInfo().log("spreadsheetId: %s | changeTitle: %s", spreadsheetId, newTitle);

    try {
      userServicesFactory().buildSpreadsheetService(spreadsheetId).setSpreadsheetTitle(newTitle);

      return true;
    } catch (IOException ioexp) {
      logger.atSevere().withStackTrace(StackSize.MEDIUM).withCause(ioexp).log(
          "Failed changing title of spreadsheet (%s) to %s", spreadsheetId, newTitle);
      return false;
    }
  }

  /**
   * Shares the given Floody Spreadsheet to the set of other users and groups using logged-in user's
   * credentials.
   *
   * @param spreadsheetId the Id of the spreadsheet to share
   * @param shareRequest the set of users/groups to share the spreadsheet with.
   * @throws IOException if the Spreadsheet API has errors.
   */
  @PostMapping("/share/{spreadsheetId}")
  public void shareSpreadsheet(
      @PathVariable String spreadsheetId, @RequestBody ShareSpreadsheetRequest shareRequest)
      throws IOException {

    logger.atInfo().log("shareRequest: %s", shareRequest);

    verifyUserHasFullAuthOrReturn(spreadsheetId);

    SharingService sharingService = robotServicesFactory().buildSharingService();

    if (shareRequest.getGroupsCount() > 0) {
      sharingService.addGroupsToFileAsEditors(spreadsheetId, shareRequest.getGroupsList());
    }

    if (shareRequest.getUsersCount() > 0) {
      sharingService.addUsersToFileAsEditors(spreadsheetId, shareRequest.getUsersList());
    }
  }
}

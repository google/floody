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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.FloodyProperties;
import com.google.floody.protobuf.SheetObjects.FloodySheet;
import com.google.floody.spreadsheet.GoogleSpreadsheetFactory;
import com.google.floody.spreadsheet.SharingService;
import java.io.IOException;

/** Micro service to create a floody spreadsheet. */
@AutoValue
public abstract class SpreadsheetCreator {

  private static final String SPREADSHEET_TITLE_PREFIX =
      "Floody - Account: {accountId}, Config: {floodlightConfigId}";
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  abstract GoogleSpreadsheetFactory spreadsheetServiceFactory();

  abstract SharingService sharingService();

  abstract FloodyProperties floodyProperties();

  abstract ImmutableList<String> floodyAdminEmails();

  public static SpreadsheetCreator create(
      GoogleSpreadsheetFactory spreadsheetServiceFactory,
      SharingService sharingService,
      FloodyProperties floodyProperties) {

    var adminEmails =
        (floodyProperties.getAdminGroupEmails() != null)
            ? ImmutableList.copyOf(floodyProperties.getAdminGroupEmails())
            : ImmutableList.<String>of();

    return new AutoValue_SpreadsheetCreator(
        spreadsheetServiceFactory, sharingService, floodyProperties, adminEmails);
  }

  /**
   * Creates a new Google spreadsheet with pre-created sheets and validation rules.
   *
   * <p>Builds a new spreadsheet with 4 sheets - Activity, Default Tags, Publisher Tags and Custom
   * Variables. Uses the floodlight configuration id as the title of the new spreadsheet.
   *
   * @param accountId the DCM Network Id to be connected to the sheet.
   * @param floodlightConfigurationId DCM floodlight configuration id, to be used as title of the
   *     spreadsheet.
   * @param userEmail the email address of the requester's email to share the new spreadsheet.
   * @return the Google spreadsheet Id of the newly created spreadsheet
   * @throws IOException when there are errors in creating or sharing spreadsheet
   */
  public FloodySheet createSpreadsheet(
      long accountId, long floodlightConfigurationId, String userEmail) throws IOException {
    checkArgument(accountId > 0, "accountId (%s) should be a positive number", accountId);
    checkArgument(
        floodlightConfigurationId > 0,
        "flConfigurationId (%s) should be a positive number",
        floodlightConfigurationId);

    // Set the newly created spreadsheet id in the service.
    var spreadsheetTile =
        SPREADSHEET_TITLE_PREFIX
            .replace("{accountId}", String.valueOf(accountId))
            .replace("{floodlightConfigId}", String.valueOf(floodlightConfigurationId));

    var spreadsheetService = spreadsheetServiceFactory().forNewSpreadsheet(spreadsheetTile);

    var spreadsheetId = spreadsheetService.getSpreadsheetId();

    // log spreadsheetId
    logger.atInfo().log("spreadsheetId: %s", spreadsheetId);

    SheetsCreator.create(spreadsheetService, floodyAdminEmails()).createAllSheets();

    // delete the default sheet
    spreadsheetService.deleteSheet(0);

    // update Developer Metadata
    DcmSpreadsheetMetaWriter.builder()
        .setSpreadsheetMetaWriter(spreadsheetService.metaWriter())
        .setAccountId(accountId)
        .setFloodlightConfigurationId(floodlightConfigurationId)
        .setSheetMetadataFloodlightConfigurationIdKey(
            floodyProperties().getSheetMetadataFloodlightConfigurationIdKey())
        .setSheetMetadataAccountIdKey(floodyProperties().getSheetMetadataAccountIdKey())
        .build()
        .writeDcmInformation();

    // share the spreadsheet to floody group & requesting User
    sharingService().addGroupsToFileAsEditors(spreadsheetId, floodyAdminEmails());
    sharingService().addUsersToFileAsEditors(spreadsheetId, ImmutableList.of(userEmail));

    return FloodySheet.newBuilder()
        .setId(spreadsheetId)
        .setName(spreadsheetTile)
        .setLink(spreadsheetService.getSpreadsheet().getSpreadsheetUrl())
        .build();
  }
}

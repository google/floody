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

import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.FloodyProperties;
import com.google.floody.protobuf.SheetObjects.FloodySheet;
import com.google.floody.protobuf.SheetObjects.SpreadsheetDcmInformation;
import com.google.floody.protobuf.UserOperations.UiSpreadsheetAuthResponse;
import com.google.floody.protobuf.UserOperations.UiSpreadsheetAuthResponse.UiSpreadsheetAuthStatus;
import com.google.floody.spreadsheet.SpreadsheetService;
import java.io.IOException;

/** Service to identify user's auth permissions for given Floody spreadsheet. */
public class UiUserAuthService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final DcmUserAccessService dcmUserAccessService;
  private final UserSpreadsheetRoleChecker userSpreadsheetRoleChecker;
  private final SpreadsheetService spreadsheetService;
  private final FloodyProperties floodyProperties;

  public UiUserAuthService(
      DcmUserAccessService dcmUserAccessService,
      UserSpreadsheetRoleChecker userSpreadsheetRoleChecker,
      SpreadsheetService spreadsheetService,
      FloodyProperties floodyProperties) {
    this.dcmUserAccessService = dcmUserAccessService;
    this.userSpreadsheetRoleChecker = userSpreadsheetRoleChecker;
    this.spreadsheetService = spreadsheetService;
    this.floodyProperties = floodyProperties;
  }

  public boolean isFullAuth() {
    return getUserAuthStatus().getStatus().equals(UiSpreadsheetAuthStatus.FULL_AUTH);
  }

  public UiSpreadsheetAuthResponse getUserAuthStatus() {

    var spreadsheetId = spreadsheetService.getSpreadsheetId();

    var responseBuilder = UiSpreadsheetAuthResponse.newBuilder();

    try {
      responseBuilder.setSpreadsheetInformation(
          FloodySheet.newBuilder()
              .setId(spreadsheetId)
              .setName(spreadsheetService.getSpreadsheetTitle()));

      SpreadsheetDcmInformation dcmInfo =
          new DcmSpreadsheetMetaReader(
                  spreadsheetService.metaReader(),
                  floodyProperties.getSheetMetadataFloodlightConfigurationIdKey(),
                  floodyProperties.getSheetMetadataAccountIdKey())
              .readDcmInformation();

      if (dcmInfo.getAccountId() == 0) {
        return responseBuilder.setStatus(UiSpreadsheetAuthStatus.NO_AUTH).build();
      }

      var userProfilesForAccount =
          dcmUserAccessService.listUserProfilesForAccount(dcmInfo.getAccountId()).getItemsList();

      responseBuilder
          .setDcmInformation(dcmInfo)
          .addAllUserDcmProfiles(userProfilesForAccount)
          .setStatus(
              buildUserAuthStatus(
                  /*userDcmAccess=*/ !userProfilesForAccount.isEmpty(),
                  /*userSpreadsheetAccess=*/ userSpreadsheetRoleChecker.isUserAccess(
                      spreadsheetId)))
          .build();
    } catch (IOException ioexp) {
      responseBuilder.setStatus(UiSpreadsheetAuthStatus.NO_AUTH);
    }

    return responseBuilder.build();
  }

  /** Returns the User's auth status based on user's access to DCM and Spreadsheet. */
  private static UiSpreadsheetAuthStatus buildUserAuthStatus(
      boolean userDcmAccess, boolean userSpreadsheetAccess) {
    if (userDcmAccess && userSpreadsheetAccess) {
      return UiSpreadsheetAuthStatus.FULL_AUTH;
    } else if (userDcmAccess) {
      return UiSpreadsheetAuthStatus.DCM_ONLY_AUTH;
    } else if (userSpreadsheetAccess) {
      return UiSpreadsheetAuthStatus.SHEET_ONLY_AUTH;
    }

    return UiSpreadsheetAuthStatus.NO_AUTH;
  }
}

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

import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.protobuf.UserOperations.DcmObjectList;
import com.google.floody.protobuf.UserOperations.RecentSheetsResponse;
import com.google.floody.protobuf.UserOperations.UiSpreadsheetAuthResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web Controller to expose Endpoints providing services to retrieve UserProfiles and check if user
 * has access to provided network/Advertiser.
 */
@RestController
@RequestMapping("/user")
public class UserOperationsController extends FloodyBaseController {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @GetMapping(value = "/recentSheets")
  public RecentSheetsResponse listUserAccessibleSheets() throws IOException {
    var userEmail = userServicesFactory().getAccountEmail();

    return robotServicesFactory().buildSheetListService().listRecentSheets(userEmail);
  }

  /**
   * The method checks the user's authorization based on the accessToken provided against the given
   * spreadsheet and DCM account.
   *
   * @param spreadsheetId the Id of Floody spreadsheet.
   * @return the Authorization level of the logged in user and linked Account Information.
   * @throws IOException when there is service initialization issues.
   */
  @GetMapping("/checkUserAuth/{spreadsheetId}")
  public UiSpreadsheetAuthResponse checkAuthorization(@PathVariable String spreadsheetId)
      throws IOException {

    return robotServicesFactory()
        .buildUiUserAuthService(spreadsheetId, userServicesFactory())
        .getUserAuthStatus();
  }

  /**
   * This method retrieves all DCM {@code FloodlightConfiguration}'s accessible to the given user
   * profile.
   *
   * @param profileId the profile id of the user
   */
  @GetMapping("/floodlightconfigs/{profileId}")
  public DcmObjectList accessibleFloodlightConfigurations(
      @PathVariable Long profileId,
      @RequestParam(value = "accountId", required = false) Long accountId)
      throws IOException {

    return userServicesFactory()
        .buildDcmUserAccessService()
        .getAllAccessibleFloodlightConfigs(profileId, accountId);
  }

  /**
   * This method retrieves all DCM {@code Account}s accessible to the given user profile Id.
   *
   * @param profileId the profile id of the user
   */
  @GetMapping("/accounts/{profileId}")
  public DcmObjectList accessibleAccounts(@PathVariable Long profileId) throws IOException {
    return userServicesFactory().buildDcmUserAccessService().getAllAccessibleAccounts(profileId);
  }

  /**
   * This method retrieves all DCM {@code UserProfile}s of the logged-in user.
   *
   * @return the list of Profile Ids and names accessible to the user
   */
  @GetMapping("/profiles")
  public DcmObjectList allUserProfiles(
      @RequestHeader(value = "accountId", required = false) Long accountId) throws IOException {
    logger.atInfo().log("accountId: %s", accountId);
    return userServicesFactory().buildDcmUserAccessService().listUserProfilesForAccount(accountId);
  }

  @GetMapping("/me")
  public Tokeninfo getLoggedInUserTokenInfo() throws IOException {
    return userServicesFactory().buildTokenInfoService().retrieveTokenInfo();
  }
}

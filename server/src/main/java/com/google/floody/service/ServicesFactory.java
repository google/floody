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

import static com.google.api.services.tagmanager.TagManagerScopes.TAGMANAGER_EDIT_CONTAINERS;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.floody.auth.CredentialServiceFactory.buildSystemCredentialService;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tagmanager.TagManager;
import com.google.floody.auth.AccessTokenCredentialService;
import com.google.floody.auth.CredentialService;
import com.google.floody.model.FloodyProperties;
import com.google.floody.spreadsheet.GoogleSpreadsheetFactory;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import com.google.floody.spreadsheet.SharingService;
import com.google.floody.spreadsheet.SharingServiceFactory;
import java.io.IOException;

/** Factory for generating any Services for the provided credentials. */
public class ServicesFactory {

  private final CredentialService credentialService;
  private final GoogleSpreadsheetFactory spreadsheetServiceFactory;
  private final SharingServiceFactory sharingServiceFactory;
  private final DcmReportingFactory dcmReportingFactory;
  private final FloodyProperties floodyProperties;

  public ServicesFactory(CredentialService credentialService, FloodyProperties floodyProperties) {
    checkNotNull(credentialService, "provide a valid CredentialService provider");

    this.floodyProperties = floodyProperties;
    this.credentialService = credentialService;
    this.spreadsheetServiceFactory = buildSpreadsheetFactory();
    this.sharingServiceFactory = buildSharingServiceFactory();
    this.dcmReportingFactory = buildDcmServiceFactory();
  }

  public CredentialService getCredentialService() {
    return credentialService;
  }

  public GoogleIdTokenVerifierService getIdTokenVerifier() {
    checkState(
        credentialService instanceof AccessTokenCredentialService,
        "Invalid Credential service: Found %s, required AccessTokenCredentialService",
        credentialService.getClass().getName());

    return new GoogleIdTokenVerifierService(
        ((AccessTokenCredentialService) credentialService).getAccessToken());
  }

  public SpreadsheetCreator buildSpreadsheetCreator() throws IOException {
    return SpreadsheetCreator.create(
        buildSpreadsheetFactory(), buildSharingService(), floodyProperties);
  }

  public UserFloodySheetsListService buildSheetListService() throws IOException {
    return new UserFloodySheetsListService(buildDriveService());
  }

  public GoogleSpreadsheetService buildSpreadsheetService(String spreadsheetId) throws IOException {
    return spreadsheetServiceFactory.forSpreadsheet(spreadsheetId);
  }

  public GoogleSpreadsheetFactory buildSpreadsheetFactory() {
    return new GoogleSpreadsheetFactory(credentialService, floodyProperties.getApplicationName());
  }

  public SharingServiceFactory buildSharingServiceFactory() {
    return new SharingServiceFactory(credentialService, floodyProperties.getApplicationName());
  }

  public DcmReportingFactory buildDcmServiceFactory() {
    return new DcmReportingFactory(credentialService, floodyProperties.getApplicationName());
  }

  public SharingService buildSharingService() throws IOException {
    return sharingServiceFactory.buildGoogleDriveSharingService();
  }

  public DcmUserAccessService buildDcmUserAccessService() throws IOException {
    return new DcmUserAccessService(buildDcmService());
  }

  public Dfareporting buildDcmService() throws IOException {
    return dcmReportingFactory.buildDcmService();
  }

  public Drive buildDriveService() throws IOException {
    return new Drive.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(DriveScopes.DRIVE))
        .setApplicationName(floodyProperties.getApplicationName())
        .build();
  }

  public OldDriveSpreadsheetsDeleter buildFileDeletor(boolean dryRun) throws IOException {
    return new OldDriveSpreadsheetsDeleter(buildDriveService(), dryRun);
  }

  public FloodyService buildFloodyService() {
    return new FloodyService(
        new GoogleSpreadsheetFactory(credentialService, floodyProperties.getApplicationName()),
        floodyProperties);
  }

  public UiUserAuthService buildUiUserAuthService(
      String spreadsheetId, ServicesFactory userServiceFactory) throws IOException {
    return new UiUserAuthService(
        userServiceFactory.buildDcmUserAccessService(),
        new UserSpreadsheetRoleChecker(buildDriveService(), userServiceFactory.getAccountEmail()),
        buildSpreadsheetService(spreadsheetId),
        floodyProperties);
  }

  public TagManager buildTagManagerService() throws IOException {
    return new TagManager.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(TAGMANAGER_EDIT_CONTAINERS))
        .build();
  }

  public OAuth2TokenInfoService buildTokenInfoService() {
    return new OAuth2TokenInfoService(credentialService, floodyProperties.getApplicationName());
  }

  public String getAccountEmail() throws IOException {
    return buildTokenInfoService().retrieveEmailAddress();
  }

  public GtmService buildGtmServiceFor(String containerId) throws IOException {
    return new GtmService(buildTagManagerService(), containerId);
  }

  public static ServicesFactory buildRobotsServicesFactory(FloodyProperties floodyProperties) {
    return new ServicesFactory(buildSystemCredentialService(), floodyProperties);
  }
}

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

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.floody.auth.CredentialService;
import java.io.IOException;

/**
 * Factory for instantiating {@link SharingService} implementations like {@link
 * GoogleDriveSharingService}.
 */
public final class SharingServiceFactory {

  private final CredentialService credentialService;

  private final String applicationName;

  /**
   * @param credentialService the credential creator for building the service
   * @param applicationName the Application name to use with API
   */
  public SharingServiceFactory(CredentialService credentialService, String applicationName) {
    this.credentialService = credentialService;
    this.applicationName = applicationName;
  }

  /**
   * Instantiates a Google Drive based Service to set file permissions.
   *
   * @return the instantiated GoogleDriveSharingService for setting file permissions
   */
  public SharingService buildGoogleDriveSharingService() throws IOException {
    return new GoogleDriveSharingService(buildGoogleDriveClient());
  }

  /**
   * Helper method to instantiate a {@link Drive} client to supply to {@link
   * GoogleDriveSharingService}.
   *
   * @return the instantiated Google Drive client
   */
  private Drive buildGoogleDriveClient() throws IOException {
    return new Drive.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(DriveScopes.DRIVE))
        .setApplicationName(applicationName)
        .build();
  }
}

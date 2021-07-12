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
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import java.io.IOException;
import org.apache.commons.lang3.tuple.ImmutablePair;

/** Concrete implementation of {@link SharingService} for Google Drive as the filesystem. */
public final class GoogleDriveSharingService implements SharingService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Drive service;

  public GoogleDriveSharingService(Drive driveService) {
    checkNotNull(driveService, "provide non-null Drive client.");
    this.service = driveService;
  }

  @Override
  public void addGroupsToFileAsEditors(String fileId, Iterable<String> groups)
      throws SharingException {
    addEditorPermissions(fileId, GoogleDrivePermissionType.GROUP, groups);
  }

  @Override
  public void addUsersToFileAsEditors(String fileId, Iterable<String> users)
      throws SharingException {
    addEditorPermissions(fileId, GoogleDrivePermissionType.USER, users);
  }

  /**
   * Add user/group as editors to a file on Google Drive.
   *
   * @param fileId the GoogleDrive id of the file to set permissions for
   * @param type define how to treat the email address as {@code GROUP} or {@code USER}
   * @param emails the list of email addresses to grant writer permission
   * @throws SharingException when there is error in setting the permissions, can be due to multiple
   *     reasons like the user does not have permission to grant further permissions OR the file
   *     doesn't exist
   */
  private void addEditorPermissions(
      String fileId, GoogleDrivePermissionType type, Iterable<String> emails)
      throws SharingException {
    checkArgument(!isBlank(fileId), "'%s' fileId is invalid", fileId);
    checkNotNull(emails, "emails is null");
    checkArgument(emails.iterator().hasNext(), "emails is empty");

    ImmutableSet.Builder<ImmutablePair<String, String>> failResultsBuilder = ImmutableSet.builder();

    try {
      BatchRequest batchRequest = service.batch();

      for (String email : emails) {
        service
            .permissions()
            .create(
                fileId,
                new Permission()
                    .setType(type.getDrivePermissionTypeValue())
                    .setRole("writer")
                    .setEmailAddress(email))
            .setSendNotificationEmail(false)
            .queue(
                batchRequest,
                new JsonBatchCallback<Permission>() {
                  @Override
                  public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                    failResultsBuilder.add(ImmutablePair.of(email, e.getMessage()));
                  }

                  @Override
                  public void onSuccess(Permission permission, HttpHeaders responseHeaders) {}
                });
      }

      batchRequest.execute();
    } catch (IOException ioexp) {
      logger.atSevere().log("failed to share the file (%s)", fileId);
      throw new SharingException("Error in sharing partnerSpreadsheet", ioexp);
    }

    var failResults = failResultsBuilder.build();

    if (failResults.size() > 0) {
      throw new SharingException("Failed to share with " + failResults.toString());
    }
  }
}

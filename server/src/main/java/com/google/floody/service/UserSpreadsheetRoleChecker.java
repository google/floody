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

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import java.io.IOException;
import java.util.function.Predicate;

public class UserSpreadsheetRoleChecker {

  public enum SpreadsheetRole {
    OWNER,
    EDITOR,
    COMMENTER,
    READER
  }

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Drive driveService;
  private final String userEmail;

  public UserSpreadsheetRoleChecker(Drive driveService, String userEmail) {
    this.driveService = driveService;
    this.userEmail = userEmail;
  }

  /** Checks if user has editor or owner permission on the spreadsheet. */
  public boolean isUserEditor(String spreadsheetId) {
    return new UserSheetAccessVerifier(spreadsheetId)
        .checkUserPermission(buildPermissionPredicate(SpreadsheetRole.EDITOR));
  }

  public boolean isUserAccess(String spreadsheetId) {
    return new UserSheetAccessVerifier(spreadsheetId)
        .checkUserPermission(buildPermissionPredicate(SpreadsheetRole.READER));
  }

  public static Predicate<Permission> buildPermissionPredicate(SpreadsheetRole role) {

    Predicate<Permission> permissionPredicate = (Permission permission) -> false;

    switch (role) {
      case READER:
        permissionPredicate = permissionPredicate.or(buildPermissionFor("reader"));

      case COMMENTER:
        permissionPredicate = permissionPredicate.or(buildPermissionFor("commenter"));

      case EDITOR:
        permissionPredicate = permissionPredicate.or(buildPermissionFor("writer"));

      case OWNER:
        permissionPredicate = permissionPredicate.or(buildPermissionFor("owner"));
    }

    return permissionPredicate;
  }

  private static Predicate<Permission> buildPermissionFor(String role) {
    return (Permission permission) -> permission.getRole().equalsIgnoreCase(role);
  }

  private final class UserSheetAccessVerifier {
    private final String spreadsheetId;

    public UserSheetAccessVerifier(String spreadsheetId) {
      this.spreadsheetId = spreadsheetId;
    }

    private boolean checkUserPermission(Predicate<Permission> permissionPredicate) {
      String nextPageToken = null;

      try {
        do {
          var listResponse =
              driveService
                  .permissions()
                  .list(spreadsheetId)
                  .setPageToken(nextPageToken)
                  .setFields("permissions(emailAddress, role)")
                  .execute();

          nextPageToken = listResponse.getNextPageToken();
          var isUserEditor =
              listResponse.getPermissions().stream()
                  .filter(permission -> permission.getEmailAddress().equals(userEmail))
                  .anyMatch(permissionPredicate);

          if (isUserEditor) {
            return true;
          }

        } while (nextPageToken != null);
      } catch (IOException ioException) {
        logger.atWarning().withStackTrace(StackSize.SMALL).log(
            "Error accessing permissions for %s", spreadsheetId);
      }

      return false;
    }
  }
}

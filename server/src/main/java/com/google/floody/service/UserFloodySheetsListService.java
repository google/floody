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

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.FloodySheets;
import com.google.floody.protobuf.SheetObjects.FloodySheet;
import com.google.floody.protobuf.UserOperations.RecentSheetsResponse;
import java.io.IOException;
import java.util.List;

/** Service to enlist Floody generated spreadsheets writable to the user. */
public final class UserFloodySheetsListService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Drive driveService;

  public UserFloodySheetsListService(Drive driveService) {
    this.driveService = driveService;
  }

  /** Returns a list of all writable Floody sheets for the provided {@code userEmail} address. */
  public RecentSheetsResponse listRecentSheets(String userEmail) {
    return RecentSheetsResponse.newBuilder()
        .addAllSpreadsheets(new RecentSheets(userEmail).list())
        .build();
  }

  private class RecentSheets {

    private static final String DRIVE_QUERY =
        "'{userEmail}' in readers and mimeType = 'application/vnd.google-apps.spreadsheet' and 'me'"
            + " in owners";

    private final String userEmail;

    public RecentSheets(String userEmail) {
      this.userEmail = userEmail;
    }

    public ImmutableList<FloodySheet> list() {
      try {
        List<File> fileList =
            driveService
                .files()
                .list()
                .setQ(buildSearchQueryString())
                .setOrderBy("modifiedTime desc")
                .setFields("files(id,name,webViewLink,modifiedTime)")
                .execute()
                .getFiles();

        if (fileList != null) {
          return fileList.stream().map(FloodySheets::fromDriveFile).collect(toImmutableList());
        }
      } catch (IOException ioexp) {
        logger.atWarning().withCause(ioexp).log("Failed to retrieve files list");
      }

      return ImmutableList.of();
    }

    private String buildSearchQueryString() {
      return DRIVE_QUERY.replace("{userEmail}", userEmail);
    }
  }
}

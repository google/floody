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

import static com.google.floody.time.Sleeper.systemSleeper;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.protobuf.FileOperations.FileOperationResult;
import com.google.floody.protobuf.FileOperations.FileOperationResultList;
import com.google.floody.time.Sleeper;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lists and deletes Drive Files owned by the service account which were last modified before a
 * given date-time.
 */
public final class OldDriveSpreadsheetsDeleter {

  private static final String DRIVE_LAST_MODIFIED_QUERY =
      "('me' in owners) and (modifiedTime < '{lastModifiedDateTime}') and (trashed = false)";
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Drive driveService;
  private final Sleeper sleeper;
  private final boolean dryRun;

  public OldDriveSpreadsheetsDeleter(Drive driveService, boolean dryRun) {
    this(driveService, dryRun, systemSleeper());
  }

  /**
   * Constructs the files deleter, the service shall not delete the files if {@code dryRun} is
   * {@code true}.
   *
   * @param driveService the drive service to use.
   * @param dryRun if-true does not delete files, only prints the files to be deleted
   * @param sleeper the sleeper to use for waiting.
   */
  @VisibleForTesting
  OldDriveSpreadsheetsDeleter(Drive driveService, boolean dryRun, Sleeper sleeper) {
    this.driveService = driveService;
    this.dryRun = dryRun;
    this.sleeper = sleeper;
  }

  /**
   * Deletes files with last modified time before provided time.
   *
   * @return details of files and deletion status.
   * @throws IOException when there is error accessing file list from Drive API
   */
  public FileOperationResultList deleteFilesLastAccessed(ZonedDateTime lastModifiedDate)
      throws IOException, InterruptedException {
    String rfc3339LastModifiedDateTime = buildRfc3339FormatTime(lastModifiedDate);
    logger.atInfo().log("deleting files modified before %s", rfc3339LastModifiedDateTime);

    String queryString =
        DRIVE_LAST_MODIFIED_QUERY.replace("{lastModifiedDateTime}", rfc3339LastModifiedDateTime);

    String nextPageToken = null;
    FileOperationResultList.Builder returnStatusBuilder = FileOperationResultList.newBuilder();
    do {
      FileList fileList =
          driveService
              .files()
              .list()
              .setQ(queryString)
              .setPageToken(nextPageToken)
              .setFields("files(id,modifiedTime),nextPageToken")
              .execute();

      nextPageToken = fileList.getNextPageToken();

      ImmutableSet<FileOperationResult> batchDeletionStatus =
          BatchFileDeleter.create(driveService, dryRun).delete(fileList.getFiles());

      returnStatusBuilder.addAllResults(batchDeletionStatus);

      // Delay to manage API rate limiting.
      sleeper.sleep(Duration.ofSeconds(5));
    } while (nextPageToken != null);

    return returnStatusBuilder.build();
  }

  private static String buildRfc3339FormatTime(ZonedDateTime lastModifiedDateTime) {
    return lastModifiedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
}

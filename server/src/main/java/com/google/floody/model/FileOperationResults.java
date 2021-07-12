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

package com.google.floody.model;

import com.google.api.services.drive.model.File;
import com.google.floody.protobuf.FileOperations.FileOperationResult;
import com.google.floody.protobuf.FileOperations.FileOperationResult.Status;

public final class FileOperationResults {

  public static FileOperationResultsBuilder success() {
    return new FileOperationResultsBuilder(Status.SUCCESS);
  }

  public static FileOperationResultsBuilder fail() {
    return new FileOperationResultsBuilder(Status.FAIL);
  }

  public static FileOperationResultsBuilder dryRun() {
    return new FileOperationResultsBuilder(Status.DRY_RUN);
  }

  public static class FileOperationResultsBuilder {

    private final Status status;

    private FileOperationResultsBuilder(Status status) {
      this.status = status;
    }

    /**
     * Convenience factory method to quickly create an operationResult for given file and status.
     *
     * @param file the Google Drive file under operation.
     * @return result object for given file and operation status.
     */
    public FileOperationResult forDriveFile(File file) {
      return FileOperationResult.newBuilder()
          .setStatus(status)
          .setSpreadsheetInformation(FloodySheets.fromDriveFile(file))
          .build();
    }
  }

  private FileOperationResults() {}
}

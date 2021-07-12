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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.model.FileOperationResults;
import com.google.floody.protobuf.FileOperations.FileOperationResult;
import java.io.IOException;
import java.util.Collection;

/**
 * Service to delete Drive files using batch API. The class is package-private to ensure
 * non-instantiation from other packages. Objects of this class should be used only once.
 */
class BatchFileDeleter {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Drive driveService;
  private final BatchRequest batchRequest;
  private final ImmutableSet.Builder<FileOperationResult> batchStatusBuilder;
  private final boolean dryRun;

  public BatchFileDeleter(Drive driveService, boolean dryRun) {
    this.driveService = driveService;
    this.batchRequest = driveService.batch();
    this.batchStatusBuilder = ImmutableSet.builder();
    this.dryRun = dryRun;
  }

  public static BatchFileDeleter create(Drive driveService, boolean dryRun) {
    return new BatchFileDeleter(driveService, dryRun);
  }

  /**
   * Deletes the provided files as a single batch.
   *
   * @return Operation status for each of the file in the batch.
   */
  public ImmutableSet<FileOperationResult> delete(Collection<File> files) {

    try {
      files.forEach(this::addFileDeleteToBatch);

      logger.atInfo().log("sending %s files for deletion.", files.size());

      if (!dryRun && files.size() > 0) {
        batchRequest.execute();
      }
      return batchStatusBuilder.build();
    } catch (IOException ioexp) {
      logger.atWarning().withStackTrace(StackSize.SMALL).log(
          "error deleting batch\n%s", ioexp.getMessage());
    }

    return makeAllFilesFailed(files);
  }

  /** Returns a failed status for all the files. */
  private static ImmutableSet<FileOperationResult> makeAllFilesFailed(Collection<File> files) {
    return files.stream()
        .map(file -> FileOperationResults.fail().forDriveFile(file))
        .collect(toImmutableSet());
  }

  private void addFileDeleteToBatch(File file) {
    try {
      DriveBatchDeleteCallBack callback = new DriveBatchDeleteCallBack(file);

      driveService.files().delete(file.getId()).queue(batchRequest, callback);

      if (dryRun) {
        callback.onDryRun();
      }
    } catch (IOException ioexp) {
      logger.atWarning().withStackTrace(StackSize.SMALL).log(
          "error adding file (%s) to batch\n%s", file.getId(), ioexp.getMessage());
    }
  }

  /**
   * Receives callback from the batch API and marks each file as success/fail in the batch status
   * builder.
   */
  private class DriveBatchDeleteCallBack extends JsonBatchCallback<Void> {

    private final File file;

    public DriveBatchDeleteCallBack(File file) {
      this.file = file;
    }

    @Override
    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
      batchStatusBuilder.add(FileOperationResults.fail().forDriveFile(file));
      logger.atWarning().log("error deleting file:\n %s", googleJsonError);
    }

    @Override
    public void onSuccess(Void aVoid, HttpHeaders httpHeaders) {
      batchStatusBuilder.add(FileOperationResults.success().forDriveFile(file));
    }

    void onDryRun() {
      batchStatusBuilder.add(FileOperationResults.dryRun().forDriveFile(file));
    }
  }
}

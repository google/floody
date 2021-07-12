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
import com.google.common.flogger.GoogleLogger;
import com.google.floody.protobuf.SheetObjects.FloodySheet;
import com.google.protobuf.util.Timestamps;
import java.text.ParseException;

public abstract class FloodySheets {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public static FloodySheet fromDriveFile(File file) {
    try {

      FloodySheet.Builder sheetBuilder = FloodySheet.newBuilder().setId(file.getId());

      if (file.getWebViewLink() != null) {
        sheetBuilder.setLink(file.getWebViewLink());
      }

      if (file.getName() != null) {
        sheetBuilder.setName(file.getName());
      }

      if (file.getModifiedTime() != null) {
        sheetBuilder.setLastModified(Timestamps.parse(file.getModifiedTime().toStringRfc3339()));
      }

      return sheetBuilder.build();
    } catch (ParseException parseException) {
      throw new RuntimeException("Unable to parse time for file: " + file.getId(), parseException);
    }
  }
}

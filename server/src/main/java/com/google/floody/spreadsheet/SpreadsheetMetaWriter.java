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

import com.google.api.services.sheets.v4.model.DeveloperMetadata;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;

public interface SpreadsheetMetaWriter {

  SpreadsheetService getSpreadsheetService();

  /**
   * Creates a new DeveloperMetadata entry in the given spreadsheet at "SPREADSHEET" level and
   * accessible to all "DOCUMENT" users. This will create a new metadata entry in the spreadsheet
   * even when an entry with the same key exists.
   *
   * @param key the metadata key for the metadata entry.
   * @param value the value of the metadata entry.
   * @return the newly created metadata entry.
   * @throws IOException in case of spreadsheet exception
   */
  DeveloperMetadata addMetadata(String key, String value) throws IOException;

  /**
   * Creates a multiple DeveloperMetadata entries in the given spreadsheet at "SPREADSHEET" level
   * and accessible to all "DOCUMENT" users.
   *
   * @param metaDataValues key-value pairs to add to the spreadsheet.*
   * @return the newly created metadata entries.
   * @throws IOException in case of spreadsheet exception
   */
  ImmutableList<DeveloperMetadata> addMetadata(ImmutableMap<String, String> metaDataValues)
      throws IOException;
}

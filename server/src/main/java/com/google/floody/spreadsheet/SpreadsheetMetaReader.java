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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import java.io.IOException;

public interface SpreadsheetMetaReader {

  SpreadsheetService getSpreadsheetService();

  /**
   * Returns the Value associated with they given key in the spreadsheet's metadata.
   *
   * @param key metadata key to read from spreadsheet's developer metadata.
   * @return the all the metadata values for the key or empty list.
   * @throws IOException in case of spreadsheet exception
   */
  ImmutableList<String> readMetadata(String key) throws IOException;

  /**
   * Returns Key-Value pairs associated with they given keys in the spreadsheet's metadata.
   *
   * <p>If a key has more than one value the returned list will contain one Pair for each of them.
   * e.g. If there are two metadata entries for Key = K1 with values V1 and V2. The returned list
   * will be [{K1,V1}, {K1,V2}].
   *
   * <p>The pair contains a null value if the key was not found in the metadata.
   *
   * @param key1 metadata key to read from spreadsheet's developer metadata.
   * @param keys all the keys to read from the metadata.
   * @return key-value pairs of the metadata (values may be null if not found).
   * @throws IOException in case of spreadsheet exception
   */
  ImmutableMultimap<String, String> readMetadata(String key1, String... keys) throws IOException;
}

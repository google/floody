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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildAddDeveloperMetadataRequest;
import static com.google.floody.spreadsheet.GoogleSpreadsheetRequestBuilder.buildSearchMetadataRequest;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CreateDeveloperMetadataResponse;
import com.google.api.services.sheets.v4.model.DeveloperMetadata;
import com.google.api.services.sheets.v4.model.MatchedDeveloperMetadata;
import com.google.api.services.sheets.v4.model.Response;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import java.io.IOException;

public class GoogleSpreadsheetMetaManager implements SpreadsheetMetaReader, SpreadsheetMetaWriter {

  private final GoogleSpreadsheetService spreadsheetService;
  private final Sheets sheetsService;

  public GoogleSpreadsheetMetaManager(GoogleSpreadsheetService spreadsheetService) {
    this.spreadsheetService = spreadsheetService;
    this.sheetsService = spreadsheetService.getSheetsService();
  }

  @Override
  public SpreadsheetService getSpreadsheetService() {
    return spreadsheetService;
  }

  @Override
  public ImmutableMultimap<String, String> readMetadata(String key, String... keys)
      throws IOException {
    String[] allKeys = new String[keys.length + 1];
    allKeys[0] = key;
    System.arraycopy(keys, 0, allKeys, 1, keys.length);

    return readAllMetaData(allKeys);
  }

  @Override
  public ImmutableList<String> readMetadata(String key) throws IOException {
    return ImmutableList.copyOf(readAllMetaData(key).values());
  }

  /**
   * Returns the key-value pairs for all keys by reading Spreadsheet's developer metadata.
   *
   * @param keys the metadata-keys to read from spreadsheet
   */
  private ImmutableMultimap<String, String> readAllMetaData(String... keys) throws IOException {

    return sheetsService
        .spreadsheets()
        .developerMetadata()
        .search(spreadsheetService.getSpreadsheetId(), buildSearchMetadataRequest(keys))
        .execute()
        .getMatchedDeveloperMetadata()
        .stream()
        .map(MatchedDeveloperMetadata::getDeveloperMetadata)
        .collect(
            toImmutableListMultimap(
                meta -> meta.getMetadataKey(), meta -> meta.getMetadataValue()));
  }

  @Override
  public DeveloperMetadata addMetadata(String key, String value) throws IOException {
    return addMetadata(ImmutableMap.of(key, value)).get(0);
  }

  @Override
  public ImmutableList<DeveloperMetadata> addMetadata(ImmutableMap<String, String> metaDataValues)
      throws IOException {

    return sheetsService
        .spreadsheets()
        .batchUpdate(
            spreadsheetService.getSpreadsheetId(), buildAddDeveloperMetadataRequest(metaDataValues))
        .execute()
        .getReplies()
        .stream()
        .map(Response::getCreateDeveloperMetadata)
        .map(CreateDeveloperMetadataResponse::getDeveloperMetadata)
        .collect(toImmutableList());
  }
}

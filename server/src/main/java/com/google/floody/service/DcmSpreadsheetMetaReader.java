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

import static java.lang.Long.parseLong;

import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.protobuf.SheetObjects.SpreadsheetDcmInformation;
import com.google.floody.spreadsheet.SpreadsheetMetaReader;
import java.io.IOException;

/** Service to read DCM information embedded into a GoogleSpreadsheet as Metadata. */
public final class DcmSpreadsheetMetaReader {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final SpreadsheetMetaReader spreadsheetMetaReader;
  private final String sheetMetadataFloodlightConfigurationIdKey;
  private final String sheetMetadataAccountIdKey;

  public DcmSpreadsheetMetaReader(
      SpreadsheetMetaReader spreadsheetMetaReader,
      String sheetMetadataFloodlightConfigurationIdKey,
      String sheetMetadataAccountIdKey) {
    this.spreadsheetMetaReader = spreadsheetMetaReader;
    this.sheetMetadataFloodlightConfigurationIdKey = sheetMetadataFloodlightConfigurationIdKey;
    this.sheetMetadataAccountIdKey = sheetMetadataAccountIdKey;
  }

  public SpreadsheetDcmInformation readDcmInformation() {
    SpreadsheetDcmInformation.Builder infoBuilder = SpreadsheetDcmInformation.newBuilder();

    try {
      var floodlightConfigurationIds =
          spreadsheetMetaReader.readMetadata(sheetMetadataFloodlightConfigurationIdKey);
      var accountIds = spreadsheetMetaReader.readMetadata(sheetMetadataAccountIdKey);

      if (accountIds.size() > 0) {
        infoBuilder.setAccountId(parseLong(accountIds.get(0)));
      }

      if (floodlightConfigurationIds.size() > 0) {
        infoBuilder.setFloodlightConfigurationId(parseLong(floodlightConfigurationIds.get(0)));
      }
    } catch (IOException ioexp) {
      logger.atWarning().withStackTrace(StackSize.MEDIUM).withCause(ioexp).log(
          "Error Reading Metadata for Spreadsheet %s",
          spreadsheetMetaReader.getSpreadsheetService().getSpreadsheetId());
    }

    return infoBuilder.build();
  }
}

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

import com.google.floody.model.FloodyProperties;
import com.google.floody.spreadsheet.GoogleSpreadsheetFactory;
import java.io.IOException;

/**
 * Defines the contract of services to be offered by FloodyService to provide a sync capability
 * between given spreadsheet and a DCM Floodlight Configuration
 */
public final class FloodyService {

  private final GoogleSpreadsheetFactory spreadsheetServiceFactory;
  private final FloodyProperties floodyProperties;

  public FloodyService(
      GoogleSpreadsheetFactory spreadsheetServiceFactory, FloodyProperties floodyProperties) {
    this.spreadsheetServiceFactory = spreadsheetServiceFactory;
    this.floodyProperties = floodyProperties;
  }

  public FloodySheetService readFromSheets() {
    return new FloodySheetService(spreadsheetServiceFactory, floodyProperties);
  }

  public DcmFloodyReaderFactory readFromDcm(DcmReportingFactory dcmReportingFactory) {
    return new DcmFloodyReaderFactory(dcmReportingFactory);
  }

  public class DcmFloodyReaderFactory {

    private final DcmReportingFactory dcmReportingFactory;

    public DcmFloodyReaderFactory(DcmReportingFactory dcmReportingFactory) {
      this.dcmReportingFactory = dcmReportingFactory;
    }

    public DcmFloodyReader forSpreadsheet(String spreadSheetId) throws IOException {
      var dcmInfo = buildDcmSpreadsheetMetaReader(spreadSheetId).readDcmInformation();

      return forFloodlightConfiguration(dcmInfo.getFloodlightConfigurationId());
    }

    public DcmFloodyReader forFloodlightConfiguration(long floodlightConfigId) throws IOException {
      return new DcmFloodyReader(dcmReportingFactory.buildDcmService(), floodlightConfigId);
    }

    private DcmSpreadsheetMetaReader buildDcmSpreadsheetMetaReader(String spreadSheetId)
        throws IOException {
      return new DcmSpreadsheetMetaReader(
          spreadsheetServiceFactory.forSpreadsheet(spreadSheetId).metaReader(),
          floodyProperties.getSheetMetadataFloodlightConfigurationIdKey(),
          floodyProperties.getSheetMetadataAccountIdKey());
    }
  }
}

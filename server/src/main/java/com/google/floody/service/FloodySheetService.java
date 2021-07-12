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

public class FloodySheetService {

  private final GoogleSpreadsheetFactory spreadsheetServiceFactory;
  private final FloodyProperties floodyProperties;

  public FloodySheetService(
      GoogleSpreadsheetFactory spreadsheetServiceFactory, FloodyProperties floodyProperties) {
    this.spreadsheetServiceFactory = spreadsheetServiceFactory;
    this.floodyProperties = floodyProperties;
  }

  public SheetsFloodyReader forSpreadsheet(String spreadsheetId) throws IOException {
    return new SheetsFloodyReader(
        spreadsheetServiceFactory.forSpreadsheet(spreadsheetId), floodyProperties);
  }
}

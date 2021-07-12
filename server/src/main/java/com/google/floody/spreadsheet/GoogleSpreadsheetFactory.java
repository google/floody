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

import static java.util.Objects.nonNull;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.floody.auth.CredentialService;
import java.io.IOException;

/**
 * Factory pattern to instantiate concrete implementations of {@link SpreadsheetService} e.g {@code
 * GoogleSpreadsheetService}
 */
public final class GoogleSpreadsheetFactory implements SpreadsheetServiceFactory {

  private final CredentialService credentialService;

  private final String applicationName;

  /**
   * @param credentialService to be used for the service
   * @param applicationName optional name to be used for making calls
   */
  public GoogleSpreadsheetFactory(CredentialService credentialService, String applicationName) {
    this.credentialService = credentialService;
    this.applicationName = applicationName;
  }

  /**
   * Provides a factory method to instantiate {@link GoogleSpreadsheetService} for using Google
   * Spreadsheets as a provider for given SpreadsheetId.
   *
   * @param spreadSheetId the Google spreadsheetId
   */
  @Override
  public GoogleSpreadsheetService forSpreadsheet(String spreadSheetId) throws IOException {
    return new GoogleSpreadsheetService(buildGoogleSheetsClient(), spreadSheetId);
  }

  /**
   * Creates a new Google spreadsheet and instantiates a service for it.
   *
   * @param spreadsheetName the name of the Google spreadsheet.
   * @return instantiated spreadsheet service
   */
  @Override
  public GoogleSpreadsheetService forNewSpreadsheet(String spreadsheetName) throws IOException {
    var sheetsService = buildGoogleSheetsClient();
    var newSpreadsheetId = createNewSpreadsheet(sheetsService, spreadsheetName);
    return new GoogleSpreadsheetService(sheetsService, newSpreadsheetId);
  }

  /**
   * Builds a new default spreadsheet and returns the identifier
   *
   * @param name of the Spreadsheet (file name or title)
   * @return the id of the spreadsheet
   * @throws IOException when there is error in creating a spreadsheet
   */
  private static String createNewSpreadsheet(Sheets sheetsService, String name) throws IOException {
    Spreadsheet spreadsheet =
        sheetsService
            .spreadsheets()
            .create(new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(name)))
            .execute();
    return spreadsheet.getSpreadsheetId();
  }

  /**
   * Build a Sheets Client using the provided credential and application name (or default name if
   * null)
   *
   * @return the Concrete Sheets client to be used for making API calls
   */
  private Sheets buildGoogleSheetsClient() throws IOException {

    return new Sheets.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(SheetsScopes.SPREADSHEETS))
        .setApplicationName(
            nonNull(applicationName) ? applicationName : "default-gtech-ads-common-spreadsheet")
        .build();
  }
}

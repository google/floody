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

import static com.google.floody.model.DefaultTagSheetHeaderInformation.DEFAULT_TAG_RANGE;
import static com.google.floody.model.DefaultTagSheetHeaderInformation.DEFAULT_TAG_SHEET_NAME;

import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.floody.model.ActivityGroupSheetHeaderInformation;
import com.google.floody.model.ActivitySheetHeaderInformation;
import com.google.floody.model.CustomVariablesSheetHeaderInformation;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.PublisherTagSheetHeaderInformation;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import com.google.floody.transforms.ActivityGroupToSpreadsheetRowTransformer;
import com.google.floody.transforms.CustomVariableToSpreadsheetRowTransformer;
import com.google.floody.transforms.DefaultTagToSpreadSheetRowTransformer;
import com.google.floody.transforms.FloodyToSpreadSheetRowTransformer;
import com.google.floody.transforms.PublisherTagToSpreadSheetRowTransformer;
import java.io.IOException;
import java.time.Period;

/**
 * Service to update spreadsheets with {@link FloodlightActivity} information based on the given
 * {@link FloodyBundle}.
 */
public final class SheetsFloodyWriter {

  private final GoogleSpreadsheetService spreadsheetService;
  private final FloodyBundle bundle;
  private final Period defaultAudienceLifespan;

  /**
   * Parameterized constructor for instantiating the writer.
   *
   * @param bundle representing the DCM FloodlightActivities of the given DCM account.
   * @param spreadsheetService the service to use for
   */
  SheetsFloodyWriter(
      FloodyBundle bundle,
      GoogleSpreadsheetService spreadsheetService,
      Period defaultAudienceLifespan) {
    this.bundle = bundle;
    this.spreadsheetService = spreadsheetService;
    this.defaultAudienceLifespan = defaultAudienceLifespan;
  }

  /**
   * Writes the {@link FloodyBundle} based floodlight, default and publisher tags in appropriate
   * sheets in the given spreadsheet.
   *
   * @throws IOException when there is an exception in spreadsheet writing.
   */
  public void sync() throws IOException {
    // Initialize spreadsheet service to use the given spreadsheetId
    exportDefaultTagsToSheet();
    exportPublisherTagsToSheet();
    exportFloodiesToSheet();
    exportCustomVariables();
    exportActivityGroups();
  }

  /** */
  private void exportFloodiesToSheet() throws IOException {
    spreadsheetService.clearData(
        ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME,
        ActivitySheetHeaderInformation.ACTIVITY_RANGE);

    spreadsheetService.storeDataWithTransform(
        ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME,
        new FloodyToSpreadSheetRowTransformer(defaultAudienceLifespan),
        bundle.getFloodies());
  }

  /** */
  private void exportDefaultTagsToSheet() throws IOException {
    spreadsheetService.clearData(DEFAULT_TAG_SHEET_NAME, DEFAULT_TAG_RANGE);
    spreadsheetService.storeDataWithTransform(
        DEFAULT_TAG_SHEET_NAME,
        new DefaultTagToSpreadSheetRowTransformer(),
        bundle.getDefaultTags());
  }

  /** */
  private void exportPublisherTagsToSheet() throws IOException {
    spreadsheetService.clearData(
        PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_NAME,
        PublisherTagSheetHeaderInformation.PUBLISHER_TAG_RANGE);

    spreadsheetService.storeDataWithTransform(
        PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_NAME,
        new PublisherTagToSpreadSheetRowTransformer(),
        bundle.getPublisherTags());
  }

  private void exportCustomVariables() throws IOException {
    spreadsheetService.clearData(
        CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_SHEET_NAME,
        CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_RANGE);

    spreadsheetService.storeDataWithTransform(
        CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_SHEET_NAME,
        new CustomVariableToSpreadsheetRowTransformer(),
        bundle.getCustomVariables());
  }

  private void exportActivityGroups() throws IOException {
    spreadsheetService.clearData(
        ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_SHEET_NAME, "A2:C");

    spreadsheetService.storeDataWithTransform(
        ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_SHEET_NAME,
        ActivityGroupToSpreadsheetRowTransformer.create(),
        bundle.getFloodyGroups().values());
  }
}

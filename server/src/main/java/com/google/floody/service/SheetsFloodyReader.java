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

import static com.google.floody.model.ActivitySheetHeaderInformation.ACTIVITY_RANGE;
import static com.google.floody.model.ActivitySheetHeaderInformation.ACTIVITY_SHEET_NAME;
import static java.lang.Long.parseLong;

import com.google.common.collect.ImmutableSet;
import com.google.floody.model.ActivityGroupSheetHeaderInformation;
import com.google.floody.model.CustomVariablesSheetHeaderInformation;
import com.google.floody.model.DefaultTagSheetHeaderInformation;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.FloodyProperties;
import com.google.floody.model.PublisherTagSheetHeaderInformation;
import com.google.floody.model.SheetCustomVariable;
import com.google.floody.model.SheetDefaultTag;
import com.google.floody.model.SheetFloody;
import com.google.floody.model.SheetPublisherTag;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import com.google.floody.transforms.ActivityGroupToSpreadsheetRowTransformer;
import com.google.floody.transforms.CustomVariableToSpreadsheetRowTransformer;
import com.google.floody.transforms.DefaultTagToSpreadSheetRowTransformer;
import com.google.floody.transforms.FloodyToSpreadSheetRowTransformer;
import com.google.floody.transforms.PublisherTagToSpreadSheetRowTransformer;
import java.io.IOException;
import java.time.Period;

public final class SheetsFloodyReader {

  private final GoogleSpreadsheetService spreadsheetService;
  private final FloodyProperties floodyProperties;

  SheetsFloodyReader(
      GoogleSpreadsheetService spreadsheetService, FloodyProperties floodyProperties) {
    this.spreadsheetService = spreadsheetService;
    this.floodyProperties = floodyProperties;
  }

  public FloodyBundleManager load() throws IOException {
    var floodlightConfigurationId =
        parseLong(
            spreadsheetService
                .metaReader()
                .readMetadata(floodyProperties.getSheetMetadataFloodlightConfigurationIdKey())
                .get(0));

    var sectionReaders = new SheetSectionReaders(floodlightConfigurationId);

    return FloodyBundleManager.builder()
        .setBundle(
            FloodyBundle.builder()
                .setFloodies(sectionReaders.readFloodies())
                .setDefaultTags(sectionReaders.readDefaultTags())
                .setPublisherTags(sectionReaders.readPublisherTags())
                .setCustomVariables(sectionReaders.readCustomVariables())
                .setFloodyGroups(sectionReaders.readFloodyGroupMap())
                .build())
        .setFloodlightConfigurationId(floodlightConfigurationId)
        .build();
  }

  private class SheetSectionReaders {

    private final long floodlightConfigurationId;

    public SheetSectionReaders(long floodlightConfigurationId) {
      this.floodlightConfigurationId = floodlightConfigurationId;
    }

    private ImmutableSet<SheetFloody> readFloodies() throws IOException {
      var sheetFloodies =
          spreadsheetService.retrieveData(
              ACTIVITY_SHEET_NAME,
              ACTIVITY_RANGE,
              new FloodyToSpreadSheetRowTransformer(
                  Period.ofDays(floodyProperties.getDefaultAudienceMembershipDurationDays())));

      return ImmutableSet.copyOf(sheetFloodies);
    }

    private ImmutableSet<SheetDefaultTag> readDefaultTags() throws IOException {
      var sheetDefaultTags =
          spreadsheetService.retrieveData(
              DefaultTagSheetHeaderInformation.DEFAULT_TAG_SHEET_NAME,
              DefaultTagSheetHeaderInformation.DEFAULT_TAG_RANGE,
              new DefaultTagToSpreadSheetRowTransformer());

      return ImmutableSet.copyOf(sheetDefaultTags);
    }

    private ImmutableSet<SheetPublisherTag> readPublisherTags() throws IOException {
      var sheetPublisherTags =
          spreadsheetService.retrieveData(
              PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SHEET_NAME,
              PublisherTagSheetHeaderInformation.PUBLISHER_TAG_RANGE,
              new PublisherTagToSpreadSheetRowTransformer());

      return ImmutableSet.copyOf(sheetPublisherTags);
    }

    private ImmutableSet<SheetCustomVariable> readCustomVariables() throws IOException {
      var sheetCustomVariables =
          spreadsheetService.retrieveData(
              CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_SHEET_NAME,
              CustomVariablesSheetHeaderInformation.CUSTOM_VARIABLE_RANGE,
              new CustomVariableToSpreadsheetRowTransformer());

      return ImmutableSet.copyOf(sheetCustomVariables);
    }

    private FloodyGroupMap readFloodyGroupMap() throws IOException {
      var sheetFloodyGroups =
          spreadsheetService.retrieveData(
              ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_SHEET_NAME,
              ActivityGroupSheetHeaderInformation.ACTIVITY_GROUP_RANGE,
              ActivityGroupToSpreadsheetRowTransformer.create(floodlightConfigurationId));

      return FloodyGroupMap.buildFor(sheetFloodyGroups);
    }
  }
}

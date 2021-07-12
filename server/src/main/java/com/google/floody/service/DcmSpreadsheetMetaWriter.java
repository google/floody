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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.api.services.sheets.v4.model.DeveloperMetadata;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.floody.spreadsheet.SpreadsheetMetaWriter;
import java.io.IOException;

@AutoValue
public abstract class DcmSpreadsheetMetaWriter {

  abstract long floodlightConfigurationId();

  abstract long accountId();

  abstract SpreadsheetMetaWriter spreadsheetMetaWriter();

  abstract String sheetMetadataFloodlightConfigurationIdKey();

  abstract String sheetMetadataAccountIdKey();

  public ImmutableList<DeveloperMetadata> writeDcmInformation() throws IOException {
    checkArgument(accountId() > 0, "provide valid accountId (%s)", accountId());
    checkArgument(
        floodlightConfigurationId() > 0,
        "provide valid floodlightConfigurationId (%s)",
        floodlightConfigurationId());

    var dcmInformation =
        ImmutableMap.<String, String>builder()
            .put(
                sheetMetadataFloodlightConfigurationIdKey(),
                String.valueOf(floodlightConfigurationId()))
            .put(sheetMetadataAccountIdKey(), String.valueOf(accountId()))
            .build();

    return spreadsheetMetaWriter().addMetadata(dcmInformation);
  }

  public static Builder builder() {
    return new AutoValue_DcmSpreadsheetMetaWriter.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setFloodlightConfigurationId(long floodlightConfigurationId);

    public abstract Builder setAccountId(long accountId);

    public abstract Builder setSpreadsheetMetaWriter(SpreadsheetMetaWriter spreadsheetMetaWriter);

    public abstract Builder setSheetMetadataFloodlightConfigurationIdKey(String value);

    public abstract Builder setSheetMetadataAccountIdKey(String value);

    public abstract DcmSpreadsheetMetaWriter build();
  }
}

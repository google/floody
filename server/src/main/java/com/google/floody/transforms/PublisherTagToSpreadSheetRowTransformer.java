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

package com.google.floody.transforms;

import static com.google.floody.model.PublisherTagSheetHeaderInformation.PUBLISHER_TAG_CODE_COLUMN;
import static com.google.floody.model.PublisherTagSheetHeaderInformation.PUBLISHER_TAG_CONVERSION_TYPE_COLUMN;
import static com.google.floody.model.PublisherTagSheetHeaderInformation.PUBLISHER_TAG_ID_COLUMN;
import static com.google.floody.model.PublisherTagSheetHeaderInformation.PUBLISHER_TAG_SITE_ID_COLUMN;
import static com.google.floody.spreadsheet.SheetUtils.buildAsStringList;
import static com.google.floody.spreadsheet.SheetUtils.toLongOrNull;
import static com.google.floody.spreadsheet.SheetUtils.toStringOrNull;

import com.google.floody.model.PublisherTagConversionType;
import com.google.floody.model.SheetPublisherTag;
import com.google.floody.spreadsheet.SpreadsheetRowTransformer;
import java.util.List;

/** Implementation for serializing and deserialize Publisher Tag from Spreadsheet. */
public class PublisherTagToSpreadSheetRowTransformer
    implements SpreadsheetRowTransformer<SheetPublisherTag> {

  @Override
  public SheetPublisherTag transformFromSheetRow(List<Object> values) {
    if ((values == null) || values.size() != 4) {
      return null;
    }

    return SheetPublisherTag.builder()
        .setId(toLongOrNull(values.get(PUBLISHER_TAG_ID_COLUMN)))
        .setSiteId(toLongOrNull(values.get(PUBLISHER_TAG_SITE_ID_COLUMN)))
        .setConversionType(
            PublisherTagConversionType.valueOf(
                toStringOrNull(values.get(PUBLISHER_TAG_CONVERSION_TYPE_COLUMN))))
        .setTag(toStringOrNull(values.get(PUBLISHER_TAG_CODE_COLUMN)))
        .build();
  }

  @Override
  public List<Object> transformToSheetRow(SheetPublisherTag entity) {
    return buildAsStringList(
        entity.getId(), entity.getSiteId(), entity.getConversionType(), entity.getTag());
  }
}

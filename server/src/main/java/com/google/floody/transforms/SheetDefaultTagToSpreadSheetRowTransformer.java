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

import static com.google.floody.spreadsheet.SheetUtils.buildAsStringList;
import static com.google.floody.spreadsheet.SheetUtils.toLongOrNull;
import static com.google.floody.spreadsheet.SheetUtils.toStringOrNull;

import com.google.floody.model.DefaultTagSheetHeaderInformation;
import com.google.floody.model.SheetDefaultTag;
import com.google.floody.spreadsheet.SpreadsheetRowTransformer;
import java.util.List;

/** Implementation for serializing and deserialize "Default Tag" sheet from Spreadsheet */
public class SheetDefaultTagToSpreadSheetRowTransformer
    implements SpreadsheetRowTransformer<SheetDefaultTag> {

  @Override
  public SheetDefaultTag transformFromSheetRow(List<Object> values) {

    // ensure the row has required number of columns
    if (values.size() != 3) {
      return null;
    }

    return SheetDefaultTag.builder()
        .setId(toLongOrNull(values.get(DefaultTagSheetHeaderInformation.DEFAULT_TAG_ID_COLUMN)))
        .setName(
            toStringOrNull(values.get(DefaultTagSheetHeaderInformation.DEFAULT_TAG_NAME_COLUMN)))
        .setTag(
            toStringOrNull(values.get(DefaultTagSheetHeaderInformation.DEFAULT_TAG_CODE_COLUMN)))
        .build();
  }

  @Override
  public List<Object> transformToSheetRow(SheetDefaultTag sheetDefaultTag) {
    return buildAsStringList(
        sheetDefaultTag.getId(), sheetDefaultTag.getName(), sheetDefaultTag.getTag());
  }
}

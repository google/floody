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

import java.util.List;
import java.util.stream.Collectors;

/** A simple implementation of {@link SpreadsheetRowTransformer} to convert all cells to Strings */
public class StringListSpreadsheetRowTransformer
    implements SpreadsheetRowTransformer<List<String>> {

  @Override
  public List<String> transformFromSheetRow(List<Object> values) {
    return values.stream().map(Object::toString).collect(Collectors.toList());
  }

  @Override
  public List<Object> transformToSheetRow(List<String> entity) {
    return entity.stream().map(item -> (Object) item).collect(Collectors.toList());
  }
}

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

/**
 * Methods to transform POJO to Spreadsheet Rows and reverse
 *
 * @param <T> the POJO class to be transformed
 */
public interface SpreadsheetRowTransformer<T> {

  /**
   * Read a list of objects representing column values from a single row of a spreadsheet and
   * transform to the given class object by mapping columns. Returns {@code null} if there is an
   * error or mismatch in given row values.
   *
   * @param values sequence of column values read from a spreadsheet row
   * @return An instance of type {@code T}, instantiated using values from the spreadsheet row.
   */
  T transformFromSheetRow(List<Object> values);

  /**
   * Transform a given model object to a sequence of Strings to be put into a spreadsheet row.
   *
   * @param entity the object to be transformed into a row
   * @return A sequence of Strings representing column data derived from the Object.
   */
  List<Object> transformToSheetRow(T entity);
}

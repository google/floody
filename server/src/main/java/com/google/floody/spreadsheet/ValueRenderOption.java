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

/** Determines how values should be rendered in the output. */
public enum ValueRenderOption {
  /**
   * Values will be calculated & formatted in the reply according to the cell's formatting.
   * Formatting is based on the spreadsheet's locale, not the requesting user's locale. For example,
   * if A1 is 1.23 and A2 is =A1 and formatted as currency, then A2 would return "$1.23".
   */
  FORMATTED_VALUE,

  /**
   * Values will be calculated, but not formatted in the reply. For example, if A1 is 1.23 and A2 is
   * =A1 and formatted as currency, then A2 would return the number 1.23.
   */
  UNFORMATTED_VALUE,

  /**
   * Values will not be calculated. The reply will include the formulas. For example, if A1 is 1.23
   * and A2 is =A1 and formatted as currency, then A2 would return "=A1".
   */
  FORMULA
}

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

/** Determines how input data should be interpreted. */
public enum ValueInputOption {
  /** The values the user has entered will not be parsed and will be stored as-is. */
  RAW,

  /**
   * The values will be parsed as if the user typed them into the UI. Numbers will stay as numbers,
   * but strings may be converted to numbers, dates, etc. following the same rules that are applied
   * when entering text into a cell via the Google Sheets UI.
   */
  USER_ENTERED
}

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

package com.google.floody.model;

import com.google.common.collect.ImmutableList;

/** Custom Variable configuration with header information, column numbers and sheet names. */
public final class CustomVariablesSheetHeaderInformation {

  public static final ImmutableList<String> CUSTOM_VARIABLE_SHEET_HEADERS =
      ImmutableList.of("U-Variable", "Name", "Type");

  public static final String CUSTOM_VARIABLE_SHEET_NAME = "Custom Variables";

  public static final String CUSTOM_VARIABLE_RANGE = "A2:C";

  public static final int UVARIABLE_COLUMN = 0;
  public static final int NAME_COLUMN = 1;
  public static final int TYPE_COLUMN = 2;
}

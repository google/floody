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

/** Default Tag Sheet's configuration with header information, column numbers and sheet names. */
public final class DefaultTagSheetHeaderInformation {

  public static final ImmutableList<String> DEFAULT_TAG_SHEET_HEADERS =
      ImmutableList.of("ID", "Name", "Tag");

  public static final String DEFAULT_TAG_SHEET_NAME = "Default Tags";

  public static final String DEFAULT_TAG_RANGE = "A2:C";

  public static final int DEFAULT_TAG_ID_COLUMN = 0;
  public static final int DEFAULT_TAG_NAME_COLUMN = 1;
  public static final int DEFAULT_TAG_CODE_COLUMN = 2;

  private DefaultTagSheetHeaderInformation() {}
}

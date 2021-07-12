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

/** Publisher Tag sheet's configuration with header information, column numbers and sheet names. */
public final class PublisherTagSheetHeaderInformation {

  public static final ImmutableList<String> PUBLISHER_TAG_SHEET_HEADERS =
      ImmutableList.of("ID", "Site ID", "Conversion Type", "Tag");

  public static final String PUBLISHER_TAG_SHEET_NAME = "Publisher Tags";

  public static final String PUBLISHER_TAG_RANGE = "A2:D";

  public static final int PUBLISHER_TAG_ID_COLUMN = 0;
  public static final int PUBLISHER_TAG_SITE_ID_COLUMN = 1;
  public static final int PUBLISHER_TAG_CONVERSION_TYPE_COLUMN = 2;
  public static final int PUBLISHER_TAG_CODE_COLUMN = 3;

  private PublisherTagSheetHeaderInformation() {}
}

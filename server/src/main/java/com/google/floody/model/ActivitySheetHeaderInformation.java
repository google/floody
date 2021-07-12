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

/**
 * Floodlight Activity Sheet's configuration with header information, column numbers and sheet
 * names.
 */
public final class ActivitySheetHeaderInformation {

  public static final ImmutableList<String> ACTIVITY_SHEET_HEADERS =
      ImmutableList.of(
          "Account ID", // A
          "Floodlight Config ID", // B
          "Floodlight Activity ID", // C
          "Update", // D
          "Activity Name", // E
          "Activity Tag String (cat=)", // F
          "Activity Group Name", // G
          "Group Tag String (type=)", // H
          "Counting Methodology", // I
          "Expected URL", // J
          "Cache Busting", // K
          "Custom Floodlight Variables Selected", // L
          "Default Tags", // M
          "Publisher Tags", // N
          "Tag Format", // O
          "Tag Type", // P
          "Status", // Q
          "Create Audience", // R
          "Audience Lifespan", // S
          "System response" // T
          );

  public static final String ACTIVITY_SHEET_NAME = "Activities";
  public static final String ACTIVITY_RANGE = "A2:S";

  public static final int ACCOUNT_ID_COLUMN = 0;
  public static final int FLOODLIGHT_CONFIG_ID_COLUMN = 1;
  public static final int FLOODLIGHT_ID_COLUMN = 2;
  public static final int FLAG_TO_UPDATE_COLUMN = 3;
  public static final int ACTIVITY_NAME_COLUMN = 4;
  public static final int ACTIVITY_TAG_CAT_STRING_COLUMN = 5;
  public static final int ACTIVITY_GROUP_NAME_COLUMN = 6;
  public static final int GROUP_TAG_TYPE_STRING_COLUMN = 7;
  public static final int COUNTING_METHODOLOGY_COLUMN = 8;
  public static final int EXPECTED_URL_COLUMN = 9;
  public static final int CACHE_BUSTING_COLUMN = 10;
  public static final int CUSTOM_FLOODLIGHT_VARIABLES_SELECTED_COLUMN = 11;
  public static final int DEFAULT_TAGS_COLUMN = 12;
  public static final int PUBLISHER_TAGS_COLUMN = 13;
  public static final int TAG_FORMAT_COLUMN = 14;
  public static final int TAG_TYPE_COLUMN = 15;
  public static final int FLOODLIGHT_STATUS_COLUMN = 16;
  public static final int CREATE_AUDIENCE_COLUMN = 17;
  public static final int AUDIENCE_LIFESPAN_COLUMN = 18;
  public static final int REMARKS_COLUMN = 19;

  private ActivitySheetHeaderInformation() {}
}

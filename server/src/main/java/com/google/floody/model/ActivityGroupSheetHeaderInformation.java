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

public class ActivityGroupSheetHeaderInformation {

  public static final String ACTIVITY_GROUP_SHEET_NAME = "Activity Groups";

  public static final String ACTIVITY_GROUP_RANGE = "A2:C";

  public static final ImmutableList<String> ACTIVITY_GROUP_SHEET_HEADERS =
      ImmutableList.of("tagString", "Name", "type");
}

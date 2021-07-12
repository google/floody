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

import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroup.GroupType;
import com.google.floody.model.SheetFloody;

public final class SheetFloodyToFloodyGroupTransformer {

  public static FloodyGroup forSheetFloody(SheetFloody sheetFloody) {
    GroupType groupType =
        FloodlightActivityCountingMethodFloodyCountingMethodAdapter.getGroupType(
            sheetFloody.getCountingMethod());

    return FloodyGroup.builder()
        .floodlightConfigurationId(sheetFloody.getFloodlightConfigurationId())
        .name(sheetFloody.getGroupName())
        .tagString(sheetFloody.getGroupTagString())
        .type(groupType)
        .build();
  }

  private SheetFloodyToFloodyGroupTransformer() {}
}

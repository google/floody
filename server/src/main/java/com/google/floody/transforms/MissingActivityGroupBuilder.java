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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.SheetFloody;

public final class MissingActivityGroupBuilder {

  private final FloodyGroupMap existingGroups;
  private final ImmutableSet<SheetFloody> sheetFloodies;

  public MissingActivityGroupBuilder(
      ImmutableCollection<FloodyGroup> existingGroups, ImmutableSet<SheetFloody> sheetFloodies) {
    this.existingGroups = FloodyGroupMap.buildFor(existingGroups);
    this.sheetFloodies = sheetFloodies;
  }

  public ImmutableSet<FloodyGroup> getMissingGroups() {
    return sheetFloodies.stream()
        .filter(this::isFloodyFromMissingGroup)
        .map(SheetFloodyToFloodyGroupTransformer::forSheetFloody)
        .collect(toImmutableSet());
  }

  private boolean isFloodyFromMissingGroup(SheetFloody floody) {
    return !existingGroups.containsGroupName(floody.getGroupName())
        && !existingGroups.containsTagString(floody.getGroupTagString());
  }
}

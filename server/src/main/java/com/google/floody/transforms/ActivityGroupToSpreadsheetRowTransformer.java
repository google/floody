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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.floody.spreadsheet.SheetUtils.toStringOrNull;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroup.GroupType;
import com.google.floody.spreadsheet.SpreadsheetRowTransformer;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class ActivityGroupToSpreadsheetRowTransformer
    implements SpreadsheetRowTransformer<FloodyGroup> {

  abstract @Nullable Long floodlightConfigurationId();

  public static ActivityGroupToSpreadsheetRowTransformer create(long floodlightConfigurationId) {
    checkArgument(floodlightConfigurationId > 0, "Provide a valid floodlightConfigurationId >0");
    return new AutoValue_ActivityGroupToSpreadsheetRowTransformer(floodlightConfigurationId);
  }

  public static ActivityGroupToSpreadsheetRowTransformer create() {
    return new AutoValue_ActivityGroupToSpreadsheetRowTransformer(null);
  }

  @Override
  public List<Object> transformToSheetRow(FloodyGroup entity) {

    return ImmutableList.of(entity.tagString(), entity.name(), entity.type().name());
  }

  @Override
  public FloodyGroup transformFromSheetRow(List<Object> values) {

    if (values == null || values.size() < 3) {
      return null;
    }

    return FloodyGroup.builder()
        .floodlightConfigurationId(floodlightConfigurationId())
        .name(toStringOrNull(values.get(0)))
        .tagString(toStringOrNull(values.get(1)))
        .type(GroupType.valueOf(toStringOrNull(values.get(2))))
        .build();
  }
}

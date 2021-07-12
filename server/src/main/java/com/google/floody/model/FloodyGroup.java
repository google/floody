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

import com.google.api.services.dfareporting.model.FloodlightActivityGroup;
import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class FloodyGroup {

  @Nullable
  public abstract Long id();

  public abstract String name();

  public abstract String tagString();

  public abstract GroupType type();

  public abstract Long floodlightConfigurationId();

  @Nullable
  public abstract String creationRemarks();

  public enum GroupType {
    COUNTER,
    SALE
  }

  public static FloodyGroup fromFloodlightActivity(FloodlightActivityGroup activityGroup) {
    return builder()
        .type(FloodyGroup.GroupType.valueOf(activityGroup.getType()))
        .tagString(activityGroup.getTagString())
        .name(activityGroup.getName())
        .floodlightConfigurationId(activityGroup.getFloodlightConfigurationId())
        .id(activityGroup.getId())
        .build();
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_FloodyGroup.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder id(@Nullable Long id);

    public abstract Builder name(String name);

    public abstract Builder tagString(String tagString);

    public abstract Builder type(GroupType type);

    public abstract Builder floodlightConfigurationId(Long floodlightConfigurationId);

    public abstract Builder creationRemarks(@Nullable String remarks);

    public abstract FloodyGroup build();
  }
}

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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Period;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a Floodlight Activity as depicted in a Google Spreadsheet. */
@AutoValue
public abstract class SheetFloody {

  @Nullable
  public abstract Long getAccountId();

  @Nullable
  public abstract Long getFloodlightConfigurationId();

  public abstract boolean isToBeUpdated();

  @Nullable
  public abstract String getGroupName();

  public abstract String getGroupTagString();

  @Nullable
  public abstract String getTagString();

  @Nullable
  public abstract Long getId();

  public abstract String getName();

  public abstract FloodyCountingMethod getCountingMethod();

  @Nullable
  public abstract String getExpectedUrl();

  public abstract CacheBustingType getCacheBustingMethod();

  public abstract TagFormat getTagFormat();

  public abstract TagType getTagType();

  @Nullable
  public abstract String getRemarks();

  public abstract boolean isAutoCreateAudience();

  @Nullable
  public abstract Period getAudienceLifespan();

  public abstract ImmutableSet<String> getCustomFloodlightVariables();

  @Nullable
  public abstract ImmutableSet<Long> getDefaultTagIds();

  @Nullable
  public abstract ImmutableSet<Long> getPublisherTagIds();

  public abstract FloodlightActivityStatus getStatus();

  public static Builder builder() {
    return new AutoValue_SheetFloody.Builder()
        .setToBeUpdated(false)
        .setCacheBustingMethod(CacheBustingType.NONE)
        .setTagFormat(TagFormat.HTML)
        .setTagType(TagType.GLOBAL_SITE_TAG)
        .setStatus(FloodlightActivityStatus.ACTIVE)
        .setCustomFloodlightVariables(ImmutableList.of())
        .setAutoCreateAudience(false);
  }

  public SheetFloody withFloodlightConfigurationId(long floodlightConfigurationId) {
    checkArgument(
        floodlightConfigurationId > 0,
        "invalid FloodlightConfiguration Id (%s)",
        floodlightConfigurationId);
    return this.toBuilder().setFloodlightConfigurationId(floodlightConfigurationId).build();
  }

  public abstract Builder toBuilder();

  /** Convenience builder class for instantiating SheetFloody. */
  @AutoValue.Builder
  public abstract static class Builder {

    // Ensure instantiation only from builder method.
    protected Builder() {}

    // Abstract Builder set methods. //
    public abstract Builder setAccountId(@Nullable Long newAccountId);

    public abstract Builder setFloodlightConfigurationId(
        @Nullable Long newFloodlightConfigurationId);

    public abstract Builder setToBeUpdated(boolean newToBeUpdated);

    public abstract Builder setGroupName(@Nullable String newGroupName);

    public abstract Builder setGroupTagString(String newGroupTagString);

    /** Tag string (cat=) */
    public abstract Builder setTagString(@Nullable String newTagString);

    public abstract Builder setId(@Nullable Long newId);

    public abstract Builder setName(String newName);

    public abstract Builder setCountingMethod(FloodyCountingMethod newCountingMethod);

    public abstract Builder setExpectedUrl(@Nullable String newExpectedUrl);

    public abstract Builder setCacheBustingMethod(CacheBustingType newCacheBustingMethod);

    public abstract Builder setTagFormat(TagFormat newTagFormat);

    public abstract Builder setTagType(TagType newTagType);

    public abstract Builder setRemarks(@Nullable String newRemarks);

    public abstract Builder setAutoCreateAudience(boolean newAutoCreateAudience);

    public abstract Builder setAudienceLifespan(@Nullable Period audienceLifespan);

    public abstract Builder setCustomFloodlightVariables(
        ImmutableSet<String> newCustomFloodlightVariables);

    public abstract Builder setDefaultTagIds(@Nullable ImmutableSet<Long> newDefaultTagIds);

    public abstract Builder setPublisherTagIds(@Nullable ImmutableSet<Long> newPublisherTagIds);

    public abstract Builder setStatus(FloodlightActivityStatus status);

    public Builder setStatus(String activityStatus) {
      return setStatus(FloodlightActivityStatus.valueOf(activityStatus));
    }

    public abstract SheetFloody build();

    // Custom Builder Set functions to simplify Object creation.

    public Builder setToBeUpdated(String toBeUpdatedString) {
      return setToBeUpdated(toBeUpdatedString != null && toBeUpdatedString.equals("Y"));
    }

    public Builder setAutoCreateAudience(String autoCreateAudience) {
      return setAutoCreateAudience(autoCreateAudience != null && autoCreateAudience.equals("Y"));
    }

    public Builder setCountingMethod(String newGroupTypeAndCountingMethod) {
      return setCountingMethod(FloodyCountingMethod.valueOf(newGroupTypeAndCountingMethod));
    }

    public Builder setCustomFloodlightVariables(Iterable<String> customVars) {
      if (customVars == null) {
        return this;
      }

      return setCustomFloodlightVariables(ImmutableSet.copyOf(customVars));
    }

    public Builder setDefaultTagIds(@Nullable Iterable<Long> newDefaultTagIds) {
      if (newDefaultTagIds == null) {
        return this;
      }

      return setDefaultTagIds(ImmutableSet.copyOf(newDefaultTagIds));
    }

    public Builder setPublisherTagIds(@Nullable Iterable<Long> newDefaultTagIds) {
      if (newDefaultTagIds == null) {
        return this;
      }

      return setPublisherTagIds(ImmutableSet.copyOf(newDefaultTagIds));
    }
  }
}

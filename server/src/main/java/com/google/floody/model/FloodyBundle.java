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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/**
 * Unified Floodlight bundle which a composition of Floodlight activities, default tags in those
 * activities and publisher tags.
 */
@AutoValue
public abstract class FloodyBundle {

  public abstract ImmutableSet<SheetFloody> getFloodies();

  public abstract ImmutableSet<SheetDefaultTag> getDefaultTags();

  public abstract ImmutableSet<SheetPublisherTag> getPublisherTags();

  public abstract ImmutableSet<SheetCustomVariable> getCustomVariables();

  public abstract FloodyGroupMap getFloodyGroups();

  public static Builder builder() {
    return new AutoValue_FloodyBundle.Builder().setCustomVariables(ImmutableSet.of());
  }

  public FloodyBundle withFloodies(ImmutableSet<SheetFloody> updatedFloodies) {
    return toBuilder().setFloodies(updatedFloodies).build();
  }

  public FloodyBundle withFloodyGroups(FloodyGroupMap floodyGroups) {
    return toBuilder().setFloodyGroups(floodyGroups).build();
  }

  abstract Builder toBuilder();

  /** Convenience Method to build SheetBundle. */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setFloodies(ImmutableSet<SheetFloody> sheetFloodies);

    public abstract Builder setDefaultTags(ImmutableSet<SheetDefaultTag> defaultTags);

    public abstract Builder setPublisherTags(ImmutableSet<SheetPublisherTag> publisherTags);

    public abstract Builder setCustomVariables(ImmutableSet<SheetCustomVariable> customVariables);

    public abstract Builder setFloodyGroups(FloodyGroupMap floodyGroups);

    public abstract FloodyBundle build();
  }
}

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
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class SheetDefaultTag {

  public abstract long getId();

  @Nullable
  public abstract String getName();

  public abstract String getTag();

  public static Builder builder() {
    return new AutoValue_SheetDefaultTag.Builder();
  }

  public static SheetDefaultTag fromDefaultTagWithId(long id, DefaultTag defaultTag) {
    return builder().setTag(defaultTag.tag()).setName(defaultTag.name()).setId(id).build();
  }

  /** Convenience Builder for Sheet Default Tag */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(long newId);

    public abstract Builder setName(@Nullable String newName);

    public abstract Builder setTag(String newTag);

    public abstract SheetDefaultTag build();
  }
}

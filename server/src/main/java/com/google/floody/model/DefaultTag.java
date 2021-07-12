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

import com.google.api.services.dfareporting.model.FloodlightActivityDynamicTag;
import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class DefaultTag {

  @Nullable
  public abstract String name();

  public abstract String tag();

  public static DefaultTag create(@Nullable String newName, String newTag) {
    return new AutoValue_DefaultTag(newName, newTag);
  }

  public static DefaultTag fromDynamicTag(FloodlightActivityDynamicTag dynamicTag) {
    return create(dynamicTag.getName(), dynamicTag.getTag());
  }
}

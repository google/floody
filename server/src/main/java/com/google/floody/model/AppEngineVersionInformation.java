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
import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

@AutoValue
public abstract class AppEngineVersionInformation {

  public static final String STAGING_VERSION = "staging";

  public abstract String getMajor();

  public abstract String getMinor();

  public abstract String getComplete();

  public boolean isStaging() {
    return Ascii.equalsIgnoreCase(getMajor(), STAGING_VERSION);
  }

  public static AppEngineVersionInformation create(String completeVersion) {
    var versionsSplit =
        ImmutableList.copyOf(
            Splitter.on(".")
                .limit(2)
                .split(Optional.ofNullable(completeVersion).orElse(StringUtils.EMPTY)));

    return (versionsSplit.size() > 1)
        ? new AutoValue_AppEngineVersionInformation(
            versionsSplit.get(0), versionsSplit.get(1), completeVersion)
        : new AutoValue_AppEngineVersionInformation(
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
  }
}

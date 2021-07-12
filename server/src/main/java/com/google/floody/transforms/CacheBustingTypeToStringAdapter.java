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

import com.google.floody.model.CacheBustingType;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Adapter class to convert DCM Floodlight Activity's cache busting type from String to enums. */
public final class CacheBustingTypeToStringAdapter {

  private CacheBustingTypeToStringAdapter() {}

  /**
   * Converts Floodlight activity's cache busting type to {@link CacheBustingType} value or {@link
   * CacheBustingType#NONE} for Sales Floodlight Activity.
   *
   * @param cacheBustingType FloodlightActivity's cache busting type
   * @return mapped value or {@link CacheBustingType#NONE}
   */
  public static CacheBustingType extractCacheBustingTypeFromActivity(
      @Nullable String cacheBustingType) {
    return CacheBustingType.valueOf(Optional.ofNullable(cacheBustingType).orElse("NONE"));
  }

  /**
   * Converts the enum to a String for use in DCM API calls.
   *
   * @return the String value of the cache-busting type or {@code null} if type is {@link
   *     CacheBustingType#NONE}
   */
  @Nullable
  public static String getStringValue(CacheBustingType cacheBustingType) {
    return cacheBustingType.equals(CacheBustingType.NONE) ? null : cacheBustingType.toString();
  }
}

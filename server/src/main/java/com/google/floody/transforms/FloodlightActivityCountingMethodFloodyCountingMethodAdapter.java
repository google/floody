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

import static com.google.floody.model.FloodlightActivityCountingMethod.ITEMS_SOLD_COUNTING;
import static com.google.floody.model.FloodlightActivityCountingMethod.SESSION_COUNTING;
import static com.google.floody.model.FloodlightActivityCountingMethod.STANDARD_COUNTING;
import static com.google.floody.model.FloodlightActivityCountingMethod.TRANSACTIONS_COUNTING;
import static com.google.floody.model.FloodlightActivityCountingMethod.UNIQUE_COUNTING;

import com.google.floody.model.FloodlightActivityCountingMethod;
import com.google.floody.model.FloodyCountingMethod;
import com.google.floody.model.FloodyGroup.GroupType;

/**
 * Adapter pattern to convert DCM floodlight activity group type and counting methods from {@link
 * FloodlightActivityCountingMethod} to {@link FloodyCountingMethod} and the other way.
 */
public final class FloodlightActivityCountingMethodFloodyCountingMethodAdapter {

  private FloodlightActivityCountingMethodFloodyCountingMethodAdapter() {}

  /**
   * Adapter pattern to extractPublisherConversionType the counting method of DCM FloodlightActivity
   * {@link FloodlightActivityCountingMethod} to {@link FloodyCountingMethod}.
   *
   * @param floodlightCountingMethod DCM Floodlight counting method
   * @return mapped Floody equivalent value or {@code NONE}
   */
  private static FloodyCountingMethod extractCountingMethod(
      FloodlightActivityCountingMethod floodlightCountingMethod) {
    switch (floodlightCountingMethod) {
      case SESSION_COUNTING:
        return FloodyCountingMethod.COUNTER_SESSIONS;
      case UNIQUE_COUNTING:
        return FloodyCountingMethod.COUNTER_UNIQUE;
      case ITEMS_SOLD_COUNTING:
        return FloodyCountingMethod.SALES_ITEMS_SOLD;
      case TRANSACTIONS_COUNTING:
        return FloodyCountingMethod.SALES_TRANSACTIONS;
      case STANDARD_COUNTING:
    }
    return FloodyCountingMethod.COUNTER_STANDARD;
  }

  public static FloodyCountingMethod extractCountingMethod(String countingMethod) {
    return extractCountingMethod(FloodlightActivityCountingMethod.valueOf(countingMethod));
  }

  /**
   * Adapter pattern to extractPublisherConversionType the counting method of {@link
   * FloodyCountingMethod} to {@link FloodlightActivityCountingMethod}.
   *
   * @param floodyCountingMethod the Floody counting method
   * @return mapped Floodlight Activity equivalent counting method
   */
  public static FloodlightActivityCountingMethod extractCountingMethod(
      FloodyCountingMethod floodyCountingMethod) {
    switch (floodyCountingMethod) {
      case COUNTER_SESSIONS:
        return SESSION_COUNTING;
      case COUNTER_STANDARD:
        return STANDARD_COUNTING;
      case COUNTER_UNIQUE:
        return UNIQUE_COUNTING;
      case SALES_ITEMS_SOLD:
        return ITEMS_SOLD_COUNTING;
      case SALES_TRANSACTIONS:
        return TRANSACTIONS_COUNTING;
      default:
        throw new IllegalArgumentException(
            "Invalid Counting Method " + floodyCountingMethod.name());
    }
  }

  public static GroupType getGroupType(FloodyCountingMethod typeAndCountingMethod) {
    switch (typeAndCountingMethod) {
      case COUNTER_UNIQUE:
      case COUNTER_STANDARD:
      case COUNTER_SESSIONS:
        return GroupType.COUNTER;
      case SALES_ITEMS_SOLD:
      case SALES_TRANSACTIONS:
        return GroupType.SALE;
      default:
        throw new IllegalArgumentException(
            "Invalid Counting Method " + typeAndCountingMethod.name());
    }
  }
}

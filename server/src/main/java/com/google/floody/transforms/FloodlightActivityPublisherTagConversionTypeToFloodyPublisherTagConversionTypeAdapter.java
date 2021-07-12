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

import static java.util.Objects.nonNull;

import com.google.api.services.dfareporting.model.FloodlightActivityPublisherDynamicTag;
import com.google.floody.model.PublisherTagConversionType;

/** Adapter to convert DCM Publisher Tag's conversion type to Floody conversion Type. */
public final
class FloodlightActivityPublisherTagConversionTypeToFloodyPublisherTagConversionTypeAdapter {

  private FloodlightActivityPublisherTagConversionTypeToFloodyPublisherTagConversionTypeAdapter() {}

  /**
   * Transforms PublisherTag's attribution type from two booleans {@code getViewThrough()} and
   * {@code getClickThrough()} into a {@link PublisherTagConversionType} enum.
   *
   * @param tag the publisher tag
   * @return the equivalent enum value
   */
  public static PublisherTagConversionType extractPublisherConversionType(
      FloodlightActivityPublisherDynamicTag tag) {

    if (nonNull(tag.getViewThrough())
        && tag.getViewThrough()
        && nonNull(tag.getClickThrough())
        && tag.getClickThrough()) {
      return PublisherTagConversionType.BOTH;
    } else if (nonNull(tag.getViewThrough()) && tag.getViewThrough()) {
      return PublisherTagConversionType.VIEW_THROUGH;
    }

    return PublisherTagConversionType.CLICK_THROUGH;
  }

  /**
   * Sets two flags {@code setViewThrough()} and {@code setClickThrough()} in the {@link
   * FloodlightActivityPublisherDynamicTag} based on the {@link PublisherTagConversionType} value
   * and returns the modified publisher tag object.
   *
   * @param pubTag the publisher tag to modify based on the conversion type
   * @param conversionType the publisher conversion attribution type
   */
  public static void configureConversionTypeInTag(
      FloodlightActivityPublisherDynamicTag pubTag, PublisherTagConversionType conversionType) {
    switch (conversionType) {
      case BOTH:
        pubTag.setClickThrough(true).setViewThrough(true);
        return;

      case VIEW_THROUGH:
        pubTag.setClickThrough(null).setViewThrough(true);
        return;

      case CLICK_THROUGH:
        pubTag.setClickThrough(true).setViewThrough(null);
    }
  }
}

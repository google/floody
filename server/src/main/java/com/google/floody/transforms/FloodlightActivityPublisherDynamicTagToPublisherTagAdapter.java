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

import static com.google.floody.transforms.FloodlightActivityPublisherTagConversionTypeToFloodyPublisherTagConversionTypeAdapter.configureConversionTypeInTag;
import static com.google.floody.transforms.FloodlightActivityPublisherTagConversionTypeToFloodyPublisherTagConversionTypeAdapter.extractPublisherConversionType;

import com.google.api.services.dfareporting.model.FloodlightActivityDynamicTag;
import com.google.api.services.dfareporting.model.FloodlightActivityPublisherDynamicTag;
import com.google.floody.model.PublisherTag;

/** Adapter pattern to convert DCM publisher tag to Floody publisher tag. */
public final class FloodlightActivityPublisherDynamicTagToPublisherTagAdapter {

  private FloodlightActivityPublisherDynamicTagToPublisherTagAdapter() {}

  /**
   * Adapter pattern to convert {@link FloodlightActivityPublisherDynamicTag} to {@link
   * PublisherTag} by remapping fields.
   *
   * @param pubTag a complete DCM Publisher Dynamic Tag
   * @return a {@link PublisherTag} object with the same information as DCM Dynamic tag
   */
  public static PublisherTag transform(FloodlightActivityPublisherDynamicTag pubTag) {
    return PublisherTag.builder()
        .conversionType(extractPublisherConversionType(pubTag))
        .siteId(pubTag.getSiteId())
        .tag(pubTag.getDynamicTag().getTag())
        .build();
  }

  /**
   * Adapter pattern to convert {@link PublisherTag} to {@link
   * FloodlightActivityPublisherDynamicTag} by remapping fields. The id fields are null in the
   * returned object.
   *
   * @param pubTag the Floody representation of Publisher tag
   * @return a completed DCM Publisher Tag with Ids nullified
   */
  public static FloodlightActivityPublisherDynamicTag transform(PublisherTag pubTag) {
    FloodlightActivityPublisherDynamicTag pubDynamicTag =
        new FloodlightActivityPublisherDynamicTag()
            .setSiteId(pubTag.siteId())
            .setDynamicTag(new FloodlightActivityDynamicTag().setTag(pubTag.tag()));
    configureConversionTypeInTag(pubDynamicTag, pubTag.conversionType());

    return pubDynamicTag;
  }
}

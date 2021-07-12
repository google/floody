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

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.DefaultTag;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.PublisherTag;
import org.apache.commons.lang3.tuple.ImmutablePair;

public final class BundleToFloodlightActivityTransformer {

  private final FloodyBundle bundle;

  public static BundleToFloodlightActivityTransformer forBundle(FloodyBundle bundle) {
    return new BundleToFloodlightActivityTransformer(bundle);
  }

  private BundleToFloodlightActivityTransformer(FloodyBundle bundle) {
    this.bundle = bundle;
  }

  public FloodyToActivityTransformer buildActivityTransformer() {
    return new FloodyToActivityTransformer(
        buildDefaultTagMapFromSet(), buildPublisherTagMapFromSet(), bundle.getFloodyGroups());
  }

  public MissingActivityGroupBuilder missingActivityGroupBuilder(
      ImmutableSet<FloodyGroup> existingGroups) {
    return new MissingActivityGroupBuilder(existingGroups, bundle.getFloodies());
  }

  private ImmutableMap<Long, DefaultTag> buildDefaultTagMapFromSet() {
    return bundle.getDefaultTags().stream()
        .map(
            sheetDefaultTag ->
                ImmutablePair.of(
                    sheetDefaultTag.getId(),
                    DefaultTag.create(sheetDefaultTag.getName(), sheetDefaultTag.getTag())))
        .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));
  }

  private ImmutableMap<Long, PublisherTag> buildPublisherTagMapFromSet() {
    return bundle.getPublisherTags().stream()
        .map(
            sheetPublisherTag ->
                ImmutablePair.of(
                    sheetPublisherTag.getId(),
                    PublisherTag.builder()
                        .conversionType(sheetPublisherTag.getConversionType())
                        .siteId(sheetPublisherTag.getSiteId())
                        .tag(sheetPublisherTag.getTag())
                        .build()))
        .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));
  }
}

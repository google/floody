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
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.floody.model.DefaultTag;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.PublisherTag;
import com.google.floody.model.SheetDefaultTag;
import com.google.floody.model.SheetFloody;
import com.google.floody.model.SheetPublisherTag;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.ImmutablePair;

/** Utility class to provide transform functions between FloodyBundle and Floody. */
public final class FloodlightActivityToBundleTransformer {

  private final ImmutableSet<FloodlightActivity> activities;

  public FloodlightActivityToBundleTransformer(ImmutableSet<FloodlightActivity> activities) {
    this.activities = activities;
  }

  public FloodyBundle.Builder getBundleBuilder() {
    ImmutableMap<PublisherTag, Long> publisherTagMap = deduplicatePublisherTags();
    ImmutableMap<DefaultTag, Long> defaultTagMap = deduplicateDefaultTags();

    ActivityToFloodyTransformer floodyTransformer =
        new ActivityToFloodyTransformer(defaultTagMap, publisherTagMap);

    ImmutableSet<SheetFloody> sheetFloodies =
        activities.stream()
            .map(floodyTransformer::buildFloodyFromActivity)
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());

    return FloodyBundle.builder()
        .setDefaultTags(buildSheetDefaultTags(defaultTagMap))
        .setPublisherTags(buildSheetPublisherTags(publisherTagMap))
        .setFloodies(sheetFloodies);
  }

  private ImmutableSet<SheetDefaultTag> buildSheetDefaultTags(
      ImmutableMap<DefaultTag, Long> defaultTagMap) {
    return defaultTagMap.entrySet().stream()
        .map(entry -> SheetDefaultTag.fromDefaultTagWithId(entry.getValue(), entry.getKey()))
        .collect(toImmutableSet());
  }

  private ImmutableSet<SheetPublisherTag> buildSheetPublisherTags(
      ImmutableMap<PublisherTag, Long> publisherTagMap) {
    return publisherTagMap.entrySet().stream()
        .map(entry -> SheetPublisherTag.fromPublisherTagWithId(entry.getValue(), entry.getKey()))
        .collect(toImmutableSet());
  }

  private ImmutableMap<DefaultTag, Long> deduplicateDefaultTags() {
    return Streams.mapWithIndex(
            activities.stream()
                .map(FloodlightActivity::getDefaultTags)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(DefaultTag::fromDynamicTag)
                .distinct(),
            (activity, index) -> ImmutablePair.of(activity, index))
        .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));
  }

  private ImmutableMap<PublisherTag, Long> deduplicatePublisherTags() {
    return Streams.mapWithIndex(
            activities.stream()
                .map(FloodlightActivity::getPublisherTags)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(FloodlightActivityPublisherDynamicTagToPublisherTagAdapter::transform)
                .distinct(),
            (pubTag, index) -> ImmutablePair.of(pubTag, index))
        .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));
  }
}

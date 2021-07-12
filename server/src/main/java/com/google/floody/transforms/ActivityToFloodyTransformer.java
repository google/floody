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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.model.DefaultTag;
import com.google.floody.model.PublisherTag;
import com.google.floody.model.SheetFloody;
import com.google.floody.model.TagFormat;
import com.google.floody.model.TagType;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ActivityToFloodyTransformer {

  private final ImmutableMap<DefaultTag, Long> defaultTagMap;
  private final ImmutableMap<PublisherTag, Long> publisherTagMap;

  public ActivityToFloodyTransformer(
      ImmutableMap<DefaultTag, Long> defaultTagMap,
      ImmutableMap<PublisherTag, Long> publisherTagMap) {
    this.defaultTagMap = defaultTagMap;
    this.publisherTagMap = publisherTagMap;
  }

  @Nullable
  public SheetFloody buildFloodyFromActivity(FloodlightActivity activity) {
    try {
      return SheetFloody.builder()
          .setAccountId(activity.getAccountId())
          .setFloodlightConfigurationId(activity.getFloodlightConfigurationId())
          .setGroupName(activity.getFloodlightActivityGroupName())
          .setGroupTagString(activity.getFloodlightActivityGroupTagString())
          .setTagString(activity.getTagString())
          .setId(activity.getId())
          .setName(activity.getName())
          .setCountingMethod(
              FloodlightActivityCountingMethodFloodyCountingMethodAdapter.extractCountingMethod(
                  activity.getCountingMethod()))
          .setExpectedUrl(activity.getExpectedUrl())
          .setCacheBustingMethod(
              CacheBustingTypeToStringAdapter.extractCacheBustingTypeFromActivity(
                  activity.getCacheBustingType()))
          .setTagFormat(TagFormat.valueOf(activity.getTagFormat()))
          .setTagType(TagType.valueOf(activity.getFloodlightTagType()))
          .setCustomFloodlightVariables(
              Optional.ofNullable(activity.getUserDefinedVariableTypes())
                  .map(ImmutableSet::copyOf)
                  .orElseGet(ImmutableSet::of))
          .setStatus(activity.getStatus())
          .setDefaultTagIds(
              // Get Default Tag ids from the map lookup
              Optional.ofNullable(activity.getDefaultTags()).orElseGet(ImmutableList::of).stream()
                  .map(DefaultTag::fromDynamicTag)
                  .map(defaultTagMap::get)
                  .collect(toImmutableSet()))
          .setPublisherTagIds(
              // Get Publisher Tag ids from the map lookup
              Optional.ofNullable(activity.getPublisherTags()).orElseGet(ImmutableList::of).stream()
                  .map(FloodlightActivityPublisherDynamicTagToPublisherTagAdapter::transform)
                  .map(publisherTagMap::get)
                  .collect(toImmutableSet()))
          .setToBeUpdated(false)
          .build();
    } catch (RuntimeException runtimeException) {
      GoogleLogger.forEnclosingClass()
          .atWarning()
          .withStackTrace(StackSize.SMALL)
          .log(
              "Error loading FloodlightActivity: %s\n(%s: %s",
              activity, runtimeException.getMessage());
      return null;
    }
  }
}

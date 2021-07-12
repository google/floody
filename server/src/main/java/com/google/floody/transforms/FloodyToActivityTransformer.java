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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.floody.transforms.FloodlightActivityCountingMethodFloodyCountingMethodAdapter.extractCountingMethod;

import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.api.services.dfareporting.model.FloodlightActivityDynamicTag;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.DefaultTag;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.PublisherTag;
import com.google.floody.model.SheetFloody;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FloodyToActivityTransformer {

  private final ImmutableMap<Long, DefaultTag> defaultTagMap;
  private final ImmutableMap<Long, PublisherTag> publisherTagMap;
  private final FloodyGroupMap groupsMap;

  private final FloodlightActivityValidator validator;

  public FloodyToActivityTransformer(
      ImmutableMap<Long, DefaultTag> defaultTagMap,
      ImmutableMap<Long, PublisherTag> publisherTagMap,
      FloodyGroupMap groupsMap) {
    this.defaultTagMap = defaultTagMap;
    this.publisherTagMap = publisherTagMap;
    this.groupsMap = groupsMap;
    this.validator = new FloodlightActivityValidator(defaultTagMap, publisherTagMap, groupsMap);
  }

  @Nullable
  public FloodlightActivity buildActivityFromFloody(
      SheetFloody sheetFloody, StringBuilder remarksBuilder) {

    if (!validator.isValidFloody(sheetFloody, remarksBuilder)) {
      return null;
    }

    return new FloodlightActivity()
        .setFloodlightConfigurationId(sheetFloody.getFloodlightConfigurationId())
        .setFloodlightActivityGroupId(groupsMap.getForFloody(sheetFloody).id())
        .setTagString(sheetFloody.getTagString())
        .setId(sheetFloody.getId())
        .setName(sheetFloody.getName())
        .setCountingMethod(extractCountingMethod(sheetFloody.getCountingMethod()).name())
        .setExpectedUrl(sheetFloody.getExpectedUrl())
        .setCacheBustingType(
            CacheBustingTypeToStringAdapter.getStringValue(sheetFloody.getCacheBustingMethod()))
        .setTagFormat(sheetFloody.getTagFormat().name())
        .setFloodlightTagType(sheetFloody.getTagType().name())
        .setUserDefinedVariableTypes(sheetFloody.getCustomFloodlightVariables().asList())
        .setStatus(sheetFloody.getStatus().name())
        .setDefaultTags(
            Optional.ofNullable(sheetFloody.getDefaultTagIds()).orElseGet(ImmutableSet::of).stream()
                .map(defaultTagMap::get)
                .map(
                    defaultTag ->
                        new FloodlightActivityDynamicTag()
                            .setTag(defaultTag.tag())
                            .setName(defaultTag.name()))
                .collect(toImmutableList()))
        .setPublisherTags(
            Optional.ofNullable(sheetFloody.getPublisherTagIds())
                .orElseGet(ImmutableSet::of)
                .stream()
                .map(publisherTagMap::get)
                .map(FloodlightActivityPublisherDynamicTagToPublisherTagAdapter::transform)
                .collect(toImmutableList()));
  }
}

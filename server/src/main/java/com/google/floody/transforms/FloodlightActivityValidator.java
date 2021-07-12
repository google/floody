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

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.collect.ImmutableMap;
import com.google.floody.model.DefaultTag;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.PublisherTag;
import com.google.floody.model.SheetFloody;
import com.google.floody.model.TagFormat;
import com.google.floody.model.TagType;

public final class FloodlightActivityValidator {

  private static final String VALID_CAT_TAG_STRING_PATTERN = "^[A-Za-z0-9-_]{1,8}$";

  private final ImmutableMap<Long, DefaultTag> defaultTagMap;
  private final ImmutableMap<Long, PublisherTag> publisherTagMap;
  private final FloodyGroupMap groupsMap;

  public FloodlightActivityValidator(
      ImmutableMap<Long, DefaultTag> defaultTagMap,
      ImmutableMap<Long, PublisherTag> publisherTagMap,
      FloodyGroupMap groupsMap) {
    this.defaultTagMap = defaultTagMap;
    this.publisherTagMap = publisherTagMap;
    this.groupsMap = groupsMap;
  }

  public boolean isValidFloody(SheetFloody floody, StringBuilder remarksBuilder) {
    boolean isValid = true;

    FloodyGroup group = groupsMap.getForFloody(floody, remarksBuilder);

    // Check if Group Exists
    if (group == null) {
      remarksBuilder
          .append("GroupTagString (")
          .append(floody.getGroupTagString())
          .append(") not present\n");
      isValid = false;
    }

    if ((group != null) && (group.id() == null)) {
      remarksBuilder.append(group.creationRemarks());
      isValid = false;
    }

    if ((group != null)
        && !FloodlightActivityCountingMethodFloodyCountingMethodAdapter.getGroupType(
                floody.getCountingMethod())
            .equals(group.type())) {
      remarksBuilder
          .append("Activity CountingType (")
          .append(floody.getCountingMethod().name())
          .append(") mismatch with Group (")
          .append(group.type().name())
          .append(")\n");
      isValid = false;
    }

    if (!isValidTagString(floody.getTagString())) {
      remarksBuilder
          .append("activityTagString(cat=) [")
          .append(floody.getTagString())
          .append("] should be empty or contain max of 8 characters between A-Z,a-z,0-9,- and _")
          .append(" characters\n");

      isValid = false;
    }

    if (isBlank(floody.getExpectedUrl())) {
      remarksBuilder.append("expected Url is empty\n");
      isValid = false;
    }

    if (!isValidTagFormat(floody)) {
      remarksBuilder.append("Invalid Tag Format. Global Site Tag supports only HTML format\n");
      isValid = false;
    }

    // check if default tags and publisher tags are valid

    if (floody.getDefaultTagIds() != null
        && !floody.getDefaultTagIds().isEmpty()
        && !defaultTagMap.keySet().containsAll(floody.getDefaultTagIds())) {
      remarksBuilder.append("One of the Default Tags was incorrect and has been dropped\n");
      isValid = false;
    }

    if (floody.getPublisherTagIds() != null
        && !floody.getPublisherTagIds().isEmpty()
        && !publisherTagMap.keySet().containsAll(floody.getPublisherTagIds())) {
      remarksBuilder.append("One of the Publisher Tags was incorrect and has been dropped\n");
      isValid = false;
    }

    if (!isValidDefaultTags(floody)) {
      remarksBuilder.append("duplicate 'Default Tags' found ");
      isValid = false;
    }

    if (!isValidPublisherTags(floody)) {
      remarksBuilder.append("Duplicate 'Publisher Tags' found");
      isValid = false;
    }

    return isValid;
  }

  private static boolean isValidTagFormat(SheetFloody floody) {
    return !(floody.getTagType().equals(TagType.GLOBAL_SITE_TAG)
        && !floody.getTagFormat().equals(TagFormat.HTML));
  }

  /** Returns true if there is no duplicate tags in selected DefaultTags. */
  private boolean isValidDefaultTags(SheetFloody floody) {
    if (floody.getDefaultTagIds() == null
        || floody.getDefaultTagIds().isEmpty()
        || defaultTagMap == null) {
      return true;
    }

    return (floody.getDefaultTagIds().size()
        == floody.getDefaultTagIds().stream()
            .map(defaultTagMap::get)
            .map(DefaultTag::tag)
            .distinct()
            .count());
  }

  /** Returns true if there is no duplicate tags in selected Publisher Tags. */
  private boolean isValidPublisherTags(SheetFloody floody) {
    if (floody.getPublisherTagIds() == null
        || floody.getPublisherTagIds().isEmpty()
        || publisherTagMap == null) {
      return true;
    }

    return (floody.getPublisherTagIds().size()
        == floody.getPublisherTagIds().stream()
            .map(publisherTagMap::get)
            .map(PublisherTag::tag)
            .distinct()
            .count());
  }

  private boolean isValidTagString(String tagString) {
    return (tagString == null)
        || tagString.isEmpty()
        || tagString.matches(VALID_CAT_TAG_STRING_PATTERN);
  }
}

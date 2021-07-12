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

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.floody.exceptions.DuplicateGroupNameRetrievalException;
import java.util.Collection;
import java.util.function.Function;

/** Keeps a map of all Floodlight Activity Groups by the tagString. */
public final class FloodyGroupMap {

  /** Map with Floodlight Activity Group's tagString as key. */
  private final ImmutableMap<String, FloodyGroup> groupMapByTagString;

  /**
   * Map with Floodlight Activity Group's name with details, handles duplicate group names through
   * use of multi-map.
   */
  private final ImmutableMultimap<String, FloodyGroup> groupMapByName;

  public static FloodyGroupMap buildFor(Collection<FloodyGroup> activityGroups) {
    return new FloodyGroupMap(activityGroups);
  }

  private FloodyGroupMap(Collection<FloodyGroup> activityGroups) {
    var groups = ImmutableSet.copyOf(activityGroups);
    groupMapByTagString = buildGroupMapByTagString(groups);
    groupMapByName = buildGroupMapByName(groups);
  }

  /** Returns all the FloodyGroups. */
  public ImmutableCollection<FloodyGroup> values() {
    return groupMapByTagString.values();
  }

  /**
   * Checks if the given FloodyGroup's tagString is present by matching the tagString.
   *
   * @param tagString the Group's tag-string to check
   * @return true if the map contains a floodyGroup with the given tagString.
   */
  public boolean containsTagString(String tagString) {
    return groupMapByTagString.containsKey(tagString);
  }

  /** Returns true if the given group name exists in the Floodlight Configuration . */
  public boolean containsGroupName(String groupName) {
    return groupMapByName.containsKey(groupName);
  }

  public FloodyGroup getForFloody(SheetFloody floody, StringBuilder remarksBuilder) {
    try {
      return getForFloody(floody);
    } catch (RuntimeException rexp) {
      remarksBuilder.append("\n").append(rexp.getMessage());
      return null;
    }
  }

  /** Returns a FloodyGroup if exists for the given tagString. */
  public FloodyGroup getForFloody(SheetFloody floody) {

    // Check through Name If groupTag String is empty (new Group)
    if (isBlank(floody.getGroupTagString())) {
      Collection<FloodyGroup> groups = groupMapByName.get(floody.getGroupName());

      // If unique then return else throw Exception.
      switch (groups.size()) {
        case 0:
          return null;
        case 1:
          return ImmutableList.copyOf(groups).get(0);
        default:
          throw new DuplicateGroupNameRetrievalException(floody.getGroupName(), groups.size());
      }
    }

    return get(floody.getGroupTagString());
  }

  public FloodyGroup get(String tagString) {
    return groupMapByTagString.get(tagString);
  }

  private static ImmutableMap<String, FloodyGroup> buildGroupMapByTagString(
      ImmutableSet<FloodyGroup> groups) {
    return groups.stream()
        .collect(ImmutableMap.toImmutableMap(FloodyGroup::tagString, Function.identity()));
  }

  private static ImmutableListMultimap<String, FloodyGroup> buildGroupMapByName(
      ImmutableSet<FloodyGroup> groups) {
    return groups.stream()
        .collect(
            ImmutableListMultimap.toImmutableListMultimap(FloodyGroup::name, Function.identity()));
  }
}

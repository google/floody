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

package com.google.floody.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.model.FloodlightActivityGroup;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.FloodyGroup;
import java.io.IOException;
import java.util.Random;

public final class DcmActivityGroupWriter {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final String VALID_GROUP_TAG_STRING_PATTERN = "^[A-Za-z0-9-_]{1,8}$";

  private final long profileId;
  private final long floodlightConfigurationId;
  private final Dfareporting dcmService;

  public static DcmActivityGroupWriterBuilder builder() {
    return new DcmActivityGroupWriterBuilder();
  }

  private DcmActivityGroupWriter(
      long profileId, long floodlightConfigurationId, Dfareporting dcmService) {
    checkArgument(profileId > 0, "Invalid Profile Id (%s)", profileId);
    checkArgument(
        floodlightConfigurationId > 0,
        "Invalid Floodlight Config Id (%s)",
        floodlightConfigurationId);

    this.dcmService = checkNotNull(dcmService, "Provide non-null DfaReporting Service");
    this.profileId = profileId;
    this.floodlightConfigurationId = floodlightConfigurationId;
  }

  public ImmutableSet<FloodyGroup> createBulkActivityGroups(
      ImmutableSet<FloodyGroup> groupsToCreate) {
    return groupsToCreate.stream().map(this::createNewActivityGroup).collect(toImmutableSet());
  }

  public FloodyGroup createNewActivityGroup(FloodyGroup floodyGroup) {
    StringBuilder remarksBuilder = new StringBuilder();
    FloodyGroup.Builder groupBuilder = floodyGroup.toBuilder();

    if (isValidFloodyGroup(floodyGroup, remarksBuilder)) {
      FloodlightActivityGroup group =
          new FloodlightActivityGroup()
              .setFloodlightConfigurationId(floodlightConfigurationId)
              .setName(extractGroupName(floodyGroup))
              .setTagString(floodyGroup.tagString())
              .setType(floodyGroup.type().name());

      try {

        group = dcmService.floodlightActivityGroups().insert(profileId, group).execute();
        groupBuilder.id(group.getId()).name(group.getName()).tagString(group.getTagString());
      } catch (IOException ioexception) {
        logger.atSevere().withCause(ioexception).log("error Creating Group: %s", floodyGroup);
        remarksBuilder.append(ioexception.getMessage()).append("\n");
      }
    }

    return groupBuilder.creationRemarks(remarksBuilder.toString()).build();
  }

  /** Returns the ActivityGroup's name or a random name for Activity Group. */
  private static String extractGroupName(FloodyGroup floodyGroup) {
    return isBlank(floodyGroup.name())
        ? ("FloodyGroup-"
            +
            // append random long number to ensure unique group name
            new Random().nextLong())
        : floodyGroup.name();
  }

  private static boolean isValidFloodyGroup(FloodyGroup group, StringBuilder remarksBuilder) {

    if (!isValidGroupTagString(group.tagString())) {
      remarksBuilder
          .append("groupTagString (type=) [")
          .append(group.tagString())
          .append("] should be empty or contain max of 8 characters between A-Z,a-z,0-9,- and _")
          .append("\n");
      return false;
    }

    return true;
  }

  private static boolean isValidGroupTagString(String tagString) {
    return (tagString == null)
        || tagString.isEmpty()
        || tagString.matches(VALID_GROUP_TAG_STRING_PATTERN);
  }

  public static final class DcmActivityGroupWriterBuilder {

    private long profileId;
    private long floodlightConfigurationId;
    private Dfareporting dcmService;

    public DcmActivityGroupWriterBuilder setProfileId(long profileId) {
      this.profileId = profileId;
      return this;
    }

    public DcmActivityGroupWriterBuilder setFloodlightConfigurationId(
        long floodlightConfigurationId) {
      this.floodlightConfigurationId = floodlightConfigurationId;
      return this;
    }

    public DcmActivityGroupWriterBuilder setDcmService(Dfareporting dcmService) {
      this.dcmService = dcmService;
      return this;
    }

    public DcmActivityGroupWriter build() {
      return new DcmActivityGroupWriter(profileId, floodlightConfigurationId, dcmService);
    }
  }
}

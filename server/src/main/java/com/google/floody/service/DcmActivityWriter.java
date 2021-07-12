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

import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.api.services.dfareporting.model.RemarketingList;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.SheetFloody;
import com.google.floody.transforms.FloodyToActivityTransformer;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Micro Service to Create/Update Floodlight Activity in DCM. */
public final class DcmActivityWriter {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Dfareporting dcmService;
  private final long profileId;
  private final FloodyToActivityTransformer transformer;

  public DcmActivityWriter(
      long profileId, Dfareporting dcmService, FloodyToActivityTransformer transformer) {
    this.dcmService = dcmService;
    this.profileId = profileId;
    this.transformer = transformer;
  }

  public SheetFloody update(SheetFloody floody) {

    // Return if Activity not to be updated.
    if (!floody.isToBeUpdated()) {
      return floody;
    }

    StringBuilder remarksBuilder = new StringBuilder();
    SheetFloody.Builder floodyBuilder = floody.toBuilder();

    try {

      FloodlightActivity updatedActivity =
          transformer.buildActivityFromFloody(floody, remarksBuilder);

      if (updatedActivity != null) {
        if (floody.getId() == null) {
          // Create New Floodlight Activity
          updatedActivity =
              dcmService.floodlightActivities().insert(profileId, updatedActivity).execute();
        } else {
          // Update activity
          updatedActivity =
              dcmService
                  .floodlightActivities()
                  .patch(profileId, floody.getId(), updatedActivity)
                  .execute();
        }

        // Update the SheetFloody with current information
        floodyBuilder
            .setId(updatedActivity.getId())
            .setToBeUpdated(false) // Flip to false if successfully updated DCM
            .setAccountId(updatedActivity.getAccountId())
            .setFloodlightConfigurationId(updatedActivity.getFloodlightConfigurationId())
            .setGroupName(updatedActivity.getFloodlightActivityGroupName())
            .setGroupTagString(updatedActivity.getFloodlightActivityGroupTagString())
            .setTagString(updatedActivity.getTagString());

        // Update remarks
        remarksBuilder
            .append("updated by Floody on ")
            .append(
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // Create Audience if requested by user.
        if (floody.isAutoCreateAudience()) {
          RemarketingList remarketingList =
              new DcmAudienceService(dcmService, profileId)
                  .createAudienceListForActivity(updatedActivity, floody.getAudienceLifespan());

          remarksBuilder
              .append("\nAudience List created (")
              .append(remarketingList.getId())
              .append(", membership: ")
              .append(remarketingList.getLifeSpan())
              .append(" days")
              .append(")\n");
        }
      }
    } catch (IOException ioexception) {
      logger.atSevere().withCause(ioexception).log(
          "error creating activity: {src: %s, tagString: %s, groupId: %s}\n",
          floody.getFloodlightConfigurationId(), floody.getTagString(), floody.getGroupTagString());
      remarksBuilder.append("\nActivity Writing had an error: ").append(ioexception.getMessage());
    }

    return floodyBuilder.setRemarks(remarksBuilder.toString()).build();
  }
}

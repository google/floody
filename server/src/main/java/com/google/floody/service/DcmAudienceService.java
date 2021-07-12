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
import com.google.api.services.dfareporting.model.ListPopulationRule;
import com.google.api.services.dfareporting.model.RemarketingList;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.time.Clock;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Micro service to provide DCM Remarketing List management methods */
public class DcmAudienceService {

  /** Provides formatting for timestamp field to be used in the audience name. */
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private static final String AUDIENCE_NAME_TEMPLATE = "{activityName}";

  private final Dfareporting dcmService;
  private final long profileId;

  /** Allows use of clock manipulation for testing */
  private final Clock clock;

  DcmAudienceService(Dfareporting dcmService, long profileId) {
    this(dcmService, profileId, Clock.systemUTC());
  }

  @VisibleForTesting
  DcmAudienceService(Dfareporting dcmService, long profileId, Clock clock) {
    this.dcmService = dcmService;
    this.profileId = profileId;
    this.clock = clock;
  }

  /**
   * Creates a new Remarketing List in DCM for the provided Activity.
   *
   * @param activity the floodlight Activity for which to create an audience list.
   * @param lifespan the membership lifespan for the given activity.
   * @return the Created Remarketing List.
   * @throws IOException when there is an issue in creating audience - Permission or Duplicate.
   */
  public RemarketingList createAudienceListForActivity(FloodlightActivity activity, Period lifespan)
      throws IOException {

    RemarketingList activityBasedAudienceList = new RemarketingList();
    String currentTimestampString = DATE_TIME_FORMATTER.format(ZonedDateTime.now(clock));

    activityBasedAudienceList
        .setAccountId(activity.getAccountId())
        .setAdvertiserId(activity.getAdvertiserId())
        .setActive(true)
        .setLifeSpan(lifespan.get(ChronoUnit.DAYS))
        .setListSource("REMARKETING_LIST_SOURCE_DFA")
        .setName(
            AUDIENCE_NAME_TEMPLATE
                .replaceAll("\\{activityName\\}", activity.getName())
                .replaceAll("\\{timestamp\\}", currentTimestampString))
        .setListPopulationRule(new ListPopulationRule().setFloodlightActivityId(activity.getId()));

    return dcmService.remarketingLists().insert(profileId, activityBasedAudienceList).execute();
  }
}

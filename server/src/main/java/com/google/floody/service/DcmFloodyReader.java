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
import com.google.api.services.dfareporting.model.FloodlightActivitiesListResponse;
import com.google.api.services.dfareporting.model.FloodlightActivity;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.FloodyBundle;
import com.google.floody.transforms.FloodlightActivityToBundleTransformer;
import java.io.IOException;

/**
 * Floodlight Activity service to build {@link FloodyBundle} from all {@link FloodlightActivity} of
 * given DCM Floodlight Configuration.
 */
public final class DcmFloodyReader {

  private final long dcmFloodlightConfigurationId;
  private final Dfareporting dfaService;

  /**
   * Constructor to build the Reader service for retrieving floodlight activities from DCM.
   *
   * @param floodlightConfigurationId the DCM FloodlightConfiguration to retrieve all activities
   */
  public DcmFloodyReader(Dfareporting service, long floodlightConfigurationId) {
    this.dcmFloodlightConfigurationId = floodlightConfigurationId;
    this.dfaService = service;
  }

  /**
   * Retrieves and transforms all {@link FloodlightActivity} items for a given
   * floodlightConfiguration.
   *
   * <p>Loads the floodlight information and build a bundle by de-duplicating the Default and
   * Publisher tags.
   *
   * @return a list of FloodlightActivity for display in Google Spreadsheet
   * @throws IOException when there is DCM API errors
   */
  public FloodyBundleManager loadFor(long dcmProfileId) throws IOException {
    ImmutableSet.Builder<FloodlightActivity> allFloodiesBuilder = ImmutableSet.builder();

    String nextPageToken = null;
    do {
      // Extract all Floodlight activities by iterating over next page tokens
      FloodlightActivitiesListResponse response =
          dfaService
              .floodlightActivities()
              .list(dcmProfileId)
              .setFloodlightConfigurationId(dcmFloodlightConfigurationId)
              .setPageToken(nextPageToken)
              .execute();

      nextPageToken = response.getNextPageToken();

      allFloodiesBuilder.addAll(response.getFloodlightActivities());
    } while (nextPageToken != null);

    var floodlightConfigReader =
        new DcmFloodlightConfigurationReaderService(
            dfaService, dcmProfileId, dcmFloodlightConfigurationId);

    FloodyBundle allFloodiesBundle =
        new FloodlightActivityToBundleTransformer(allFloodiesBuilder.build())
            .getBundleBuilder()
            .setCustomVariables(floodlightConfigReader.retrieveAllCustomVariables())
            .setFloodyGroups(floodlightConfigReader.retrieveAllActivityGroupsMap())
            .build();

    return FloodyBundleManager.builder()
        .setBundle(allFloodiesBundle)
        .setProfileId(dcmProfileId)
        .setFloodlightConfigurationId(dcmFloodlightConfigurationId)
        .build();
  }
}

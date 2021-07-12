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
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.model.FloodlightActivityGroupsListResponse;
import com.google.api.services.dfareporting.model.UserDefinedVariableConfiguration;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.SheetCustomVariable;
import com.google.floody.transforms.CustomVariableUserDefinedVariableConfigurationAdapter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Micro-service to retrieve Custom User defined variable in a given DCM Floodlight Configuration.
 */
public final class DcmFloodlightConfigurationReaderService {

  private final Dfareporting service;
  private final long profileId;
  @Nullable private final Long floodlightConfigurationId;

  public DcmFloodlightConfigurationReaderService(
      Dfareporting service, long profileId, long floodlightConfigurationId) {
    checkArgument(profileId > 0, "profileId should be positive");

    this.service = service;
    this.profileId = profileId;
    this.floodlightConfigurationId = floodlightConfigurationId;
  }

  /** Returns a map of Group Tag String (type=) and its Id. */
  public ImmutableSet<FloodyGroup> retrieveAllActivityGroups() throws IOException {
    ImmutableSet.Builder<FloodyGroup> floodyGroupBuilder = ImmutableSet.builder();

    String nextPageToken = null;

    do {
      FloodlightActivityGroupsListResponse groupsListResponse =
          service
              .floodlightActivityGroups()
              .list(profileId)
              .setFloodlightConfigurationId(floodlightConfigurationId)
              .setPageToken(nextPageToken)
              .execute();
      nextPageToken = groupsListResponse.getNextPageToken();

      groupsListResponse
          .getFloodlightActivityGroups()
          .forEach(group -> floodyGroupBuilder.add(FloodyGroup.fromFloodlightActivity(group)));
    } while (nextPageToken != null);

    return floodyGroupBuilder.build();
  }

  /** Returns a FloodyGroupMap of all existing Activity Groups. */
  public FloodyGroupMap retrieveAllActivityGroupsMap() throws IOException {
    return FloodyGroupMap.buildFor(retrieveAllActivityGroups());
  }

  /**
   * Retrieves the custom User defined variables' user specified names.
   *
   * @return All mappings between DCM U-Variable and the reporting name
   * @throws IOException if error in fetching results from DCM API
   */
  public ImmutableSet<SheetCustomVariable> retrieveAllCustomVariables() throws IOException {
    checkArgument(
        floodlightConfigurationId != null && floodlightConfigurationId > 0,
        "floodlightConfigurationId is invalid");

    Optional<List<UserDefinedVariableConfiguration>> userDefinedVariableConfigurations =
        Optional.ofNullable(
            service
                .floodlightConfigurations()
                .get(profileId, floodlightConfigurationId)
                .setFields("userDefinedVariableConfigurations")
                .execute()
                .getUserDefinedVariableConfigurations());

    return userDefinedVariableConfigurations
        .map(
            uVariables ->
                uVariables.stream()
                    .map(CustomVariableUserDefinedVariableConfigurationAdapter::transform)
                    .collect(toImmutableSet()))
        .orElseGet(ImmutableSet::of);
  }
}

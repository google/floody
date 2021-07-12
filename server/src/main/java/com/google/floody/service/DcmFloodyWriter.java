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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.api.services.dfareporting.Dfareporting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.FloodyGroup;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.SheetFloody;
import com.google.floody.transforms.BundleToFloodlightActivityTransformer;
import java.io.IOException;

public final class DcmFloodyWriter {

  private final FloodyBundle bundle;
  private final Dfareporting dcmService;
  private final long floodlightConfigurationId;
  private final long dcmProfileId;

  public DcmFloodyWriter(
      long floodlightConfigurationId,
      long dcmProfileId,
      FloodyBundle bundle,
      Dfareporting dcmService) {
    this.floodlightConfigurationId = floodlightConfigurationId;
    this.dcmProfileId = dcmProfileId;
    this.bundle = bundle;
    this.dcmService = dcmService;
  }

  public FloodyBundleManager sync() throws IOException {

    var processor = new FloodiesProcessor(dcmProfileId);

    var updatedGroups = processor.createMissingAndCombineExistingGroups();
    var updatedFloodies = processor.processFloodies(updatedGroups);

    // TODO(anantd) - Export clears custom variables - Retrieve Custom FL Variables and pass on
    return FloodyBundleManager.builder()
        .setProfileId(dcmProfileId)
        .setFloodlightConfigurationId(floodlightConfigurationId)
        .setBundle(bundle.withFloodies(updatedFloodies).withFloodyGroups(updatedGroups))
        .build();
  }

  private class FloodiesProcessor {

    final long profileId;

    public FloodiesProcessor(long profileId) {
      this.profileId = profileId;
    }

    private FloodyGroupMap createMissingAndCombineExistingGroups() throws IOException {
      ImmutableSet<FloodyGroup> existingGroups = retrieveExistingGroupsFromDcm();

      return FloodyGroupMap.buildFor(
          Sets.union(existingGroups, createMissingActivityGroups(existingGroups)).immutableCopy());
    }

    private ImmutableSet<SheetFloody> processFloodies(FloodyGroupMap allGroups) {
      DcmActivityWriter activityProcessor =
          new DcmActivityWriter(
              profileId,
              dcmService,
              BundleToFloodlightActivityTransformer.forBundle(bundle.withFloodyGroups(allGroups))
                  .buildActivityTransformer());

      return bundle.getFloodies().stream().map(activityProcessor::update).collect(toImmutableSet());
    }

    private ImmutableSet<FloodyGroup> retrieveExistingGroupsFromDcm() throws IOException {
      return new DcmFloodlightConfigurationReaderService(
              dcmService, profileId, floodlightConfigurationId)
          .retrieveAllActivityGroups();
    }

    private ImmutableSet<FloodyGroup> createMissingActivityGroups(
        ImmutableSet<FloodyGroup> existingGroups) throws IOException {
      ImmutableSet<FloodyGroup> missingGroups =
          BundleToFloodlightActivityTransformer.forBundle(bundle)
              .missingActivityGroupBuilder(existingGroups)
              .getMissingGroups();

      return DcmActivityGroupWriter.builder()
          .setDcmService(dcmService)
          .setProfileId(profileId)
          .setFloodlightConfigurationId(floodlightConfigurationId)
          .build()
          .createBulkActivityGroups(missingGroups);
    }
  }
}

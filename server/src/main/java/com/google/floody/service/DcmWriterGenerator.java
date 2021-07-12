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
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.FloodyGroupMap;
import com.google.floody.model.SheetFloody;
import java.io.IOException;

public final class DcmWriterGenerator {

  private final FloodyBundle bundle;
  private final Dfareporting dcmService;

  private DcmWriterGenerator(FloodyBundle bundle, Dfareporting dcmService) {

    this.bundle = bundle;
    this.dcmService = dcmService;
  }

  private DcmFloodyWriter buildDcmFloodyWriter(long dcmProfileId, long floodlightConfigurationId)
      throws IOException {

    var floodlightConfigReader =
        new DcmFloodlightConfigurationReaderService(
            dcmService, dcmProfileId, floodlightConfigurationId);

    FloodyGroupMap existingGroupMap =
        FloodyGroupMap.buildFor(floodlightConfigReader.retrieveAllActivityGroups());

    return new DcmFloodyWriter(
        floodlightConfigurationId,
        dcmProfileId,
        bundle.withFloodyGroups(existingGroupMap),
        dcmService);
  }

  public static class Builder {

    private final Dfareporting dcmService;
    private final FloodyBundle bundle;
    private long floodlightConfigurationId;

    Builder(FloodyBundle bundle, Dfareporting dcmService) {
      this.bundle = bundle;
      this.dcmService = dcmService;
    }

    public Builder forFloodlightConfiguration(long floodlightConfigurationId) {
      this.floodlightConfigurationId = floodlightConfigurationId;
      return this;
    }

    public DcmFloodyWriter buildDcmWriter(long dcmProfileId) throws IOException {
      checkArgument(
          floodlightConfigurationId > 0,
          "floodlightConfigurationId (%s) should be a positive number",
          floodlightConfigurationId);

      return new DcmWriterGenerator(
              bundle.withFloodies(getAllFloodiesWithFloodlightConfigurationId()), dcmService)
          .buildDcmFloodyWriter(dcmProfileId, floodlightConfigurationId);
    }

    /** Returns transformed activities with given floodlight configurationId. */
    private ImmutableSet<SheetFloody> getAllFloodiesWithFloodlightConfigurationId() {
      return bundle.getFloodies().stream()
          .map(this::buildSheetFloodyWithFloodlightConfiguration)
          .collect(toImmutableSet());
    }

    private SheetFloody buildSheetFloodyWithFloodlightConfiguration(SheetFloody sheetFloody) {
      return sheetFloody.withFloodlightConfigurationId(floodlightConfigurationId);
    }
  }
}

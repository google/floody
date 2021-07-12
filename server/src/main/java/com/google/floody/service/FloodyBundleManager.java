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

import com.google.api.services.dfareporting.Dfareporting;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.GtmExport;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import java.time.Period;

public final class FloodyBundleManager {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final FloodyBundle bundle;
  private final long floodlightConfigurationId;
  private final Period defaultAudienceLifespan;

  public static FloodyBundleManagerBuilder builder() {
    return new FloodyBundleManagerBuilder();
  }

  private FloodyBundleManager(
      FloodyBundle bundle,
      long profileId,
      long floodlightConfigurationId,
      Period defaultAudienceLifespan) {
    checkArgument(
        floodlightConfigurationId > 0,
        "Provide valid floodlightConfigurationId (%s)",
        floodlightConfigurationId);

    this.bundle = checkNotNull(bundle, "bundle should not be null");
    this.floodlightConfigurationId = floodlightConfigurationId;
    this.defaultAudienceLifespan = defaultAudienceLifespan;

    logger.atInfo().log(
        "retrieved (for_dcm_profile:%s) - activities (%s), defaultTags (%s), publisherTags (%s),"
            + " customVariables (%s) floodyGroups(%s)",
        profileId,
        bundle.getFloodies().size(),
        bundle.getDefaultTags().size(),
        bundle.getPublisherTags().size(),
        bundle.getCustomVariables().size(),
        bundle.getFloodyGroups().values().size());
  }

  public FloodyBundle getBundle() {
    return bundle;
  }

  public GtmRequestWriter.Builder toGtmRequestWriter(
      ObjectifySaverService<GtmExport> saverService) {
    return GtmRequestWriter.builder()
        .setFloodlightConfigurationId(floodlightConfigurationId)
        .setBundle(bundle)
        .setSaverService(saverService);
  }

  /**
   * Returns a service to write floodlight bundle to a spreadsheet.
   *
   * @param spreadsheetService the service to build connection to the spreadsheet
   */
  public SheetsFloodyWriter toSheets(GoogleSpreadsheetService spreadsheetService) {
    return new SheetsFloodyWriter(bundle, spreadsheetService, defaultAudienceLifespan);
  }

  public DcmWriterGenerator.Builder toDcm(Dfareporting dfareportingService) {
    return new DcmWriterGenerator.Builder(bundle, dfareportingService)
        .forFloodlightConfiguration(floodlightConfigurationId);
  }

  /** Convenience Bundle class. */
  public static final class FloodyBundleManagerBuilder {

    private FloodyBundle bundle;
    private long profileId;
    private long floodlightConfigurationId;
    private Period defaultAudienceLifespan;

    public FloodyBundleManagerBuilder setBundle(FloodyBundle bundle) {
      this.bundle = bundle;
      return this;
    }

    public FloodyBundleManagerBuilder setProfileId(long profileId) {
      this.profileId = profileId;
      return this;
    }

    public FloodyBundleManagerBuilder setFloodlightConfigurationId(long floodlightConfigurationId) {
      this.floodlightConfigurationId = floodlightConfigurationId;
      return this;
    }

    public FloodyBundleManagerBuilder setDefaultAudienceLifespan(Period defaultAudienceLifespan) {
      this.defaultAudienceLifespan = defaultAudienceLifespan;
      return this;
    }

    public FloodyBundleManagerBuilder setDefaultAudienceLifespanDays(
        int defaultAudienceLifespanDays) {
      return setDefaultAudienceLifespan(Period.ofDays(defaultAudienceLifespanDays));
    }

    public FloodyBundleManager build() {
      return new FloodyBundleManager(
          bundle, profileId, floodlightConfigurationId, defaultAudienceLifespan);
    }
  }
}

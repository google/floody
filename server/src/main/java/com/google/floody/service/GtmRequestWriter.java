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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.floody.model.FloodyBundle;
import com.google.floody.model.GtmExport;
import com.google.floody.model.GtmFloodlightActivity;
import com.google.floody.model.SheetFloody;
import com.google.floody.protobuf.GtmOperations.GtmExportRequest;
import com.google.floody.transforms.SheetFloodyToGtmFloodlightActivityTransformer;

/** Service to create a {@link GtmExport} object and store it in Datastore. */
@AutoValue
public abstract class GtmRequestWriter {

  abstract FloodyBundle bundle();

  abstract long floodlightConfigurationId();

  abstract ObjectifySaverService<GtmExport> saverService();

  abstract GtmExportRequest operationRequest();

  abstract String requesterEmail();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setBundle(FloodyBundle value);

    public abstract Builder setFloodlightConfigurationId(long floodlightConfigurationId);

    public abstract Builder setSaverService(ObjectifySaverService<GtmExport> value);

    public abstract Builder setOperationRequest(GtmExportRequest value);

    public abstract Builder setRequesterEmail(String value);

    public abstract GtmRequestWriter build();

    public final GtmRequestWriter forRequest(GtmExportRequest request, String requesterEmail) {
      return setOperationRequest(request).setRequesterEmail(requesterEmail).build();
    }
  }

  public static Builder builder() {
    return new AutoValue_GtmRequestWriter.Builder();
  }

  public FloodyBundleManager sync() {

    var exportMaker = new GtmExportMaker();

    var updatedFloodies =
        bundle().getFloodies().stream().map(exportMaker::processFloody).collect(toImmutableSet());

    saverService().save(exportMaker.make());

    return FloodyBundleManager.builder()
        .setFloodlightConfigurationId(floodlightConfigurationId())
        .setBundle(bundle().withFloodies(updatedFloodies))
        .build();
  }

  private class GtmExportMaker {

    private final ImmutableCollection.Builder<GtmFloodlightActivity> selectedFloodiesBuilder;

    public GtmExportMaker() {
      this.selectedFloodiesBuilder = ImmutableList.builder();
    }

    private SheetFloody processFloody(SheetFloody floody) {
      if (!floody.isToBeUpdated()) {
        return floody;
      }

      selectedFloodiesBuilder.add(SheetFloodyToGtmFloodlightActivityTransformer.transform(floody));

      return floody.toBuilder().setToBeUpdated(false).build();
    }

    private GtmExport make() {
      return GtmExport.builder()
          .setGtmContainerId(operationRequest().getGtmContainerId())
          .setApproverEmails(operationRequest().getApproverEmailsList())
          .setSpreadsheetId(operationRequest().getSpreadsheetId())
          .setRequesterMessage(operationRequest().getRequesterMessage())
          .setFloodlightActivities(selectedFloodiesBuilder.build())
          .setRequesterEmail(requesterEmail())
          .setActionInformation(null)
          .build();
    }
  }
}

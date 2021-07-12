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

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.tagmanager.TagManager;
import com.google.api.services.tagmanager.model.Account;
import com.google.api.services.tagmanager.model.Container;
import com.google.api.services.tagmanager.model.Tag;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.exceptions.GtmContainerNotFoundException;
import com.google.floody.model.GtmExport;
import com.google.floody.model.GtmFloodlightActivity;
import com.google.floody.protobuf.GtmOperations.GtmTagOperationResult;
import com.google.floody.transforms.GtmTagTransformer;
import java.io.IOException;
import java.util.Optional;

/** Provides GTM Operations for instantiating Inserting service. */
public final class GtmService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final TagManager tagManagerService;
  private final String containerId;

  public GtmService(TagManager tagManagerService, String containerId) {
    this.tagManagerService = tagManagerService;
    this.containerId = containerId;
  }

  public GtmBulkFloodlightInsert insertTagsFor(GtmExport gtmExport) throws IOException {
    Optional<Container> container = findContainerIdFromPublicContainerId(containerId);

    if (container.isPresent()) {
      return new GtmBulkFloodlightInsert(gtmExport, container.get());
    } else {
      throw new GtmContainerNotFoundException(containerId);
    }
  }

  private Optional<Container> findContainerIdFromPublicContainerId(String publicContainerId)
      throws IOException {
    return listAccounts().stream()
        .map(Account::getAccountId)
        .flatMap(accountId -> listContainers(accountId).stream())
        .filter(container -> container.getPublicId().equals(publicContainerId))
        .findFirst();
  }

  private ImmutableSet<Container> listContainers(String accountId) {
    try {
      return ImmutableSet.copyOf(
          tagManagerService.accounts().containers().list(accountId).execute().getContainers());
    } catch (IOException ioExp) {
      logger.atWarning().withCause(ioExp).log();
      return ImmutableSet.of();
    }
  }

  private ImmutableSet<Account> listAccounts() throws IOException {
    return ImmutableSet.copyOf(tagManagerService.accounts().list().execute().getAccounts());
  }

  /** Helper class to create bulk GTM insert requests by queuing the request. */
  public class GtmBulkFloodlightInsert {

    private GtmExport gtmExport;
    private final Container gtmContainer;
    private final ImmutableSet.Builder<GtmTagOperationResult> tagOperationResultBuilder;

    public GtmBulkFloodlightInsert(GtmExport gtmExport, Container gtmContainer) {
      this.gtmExport = gtmExport;
      this.gtmContainer = gtmContainer;
      tagOperationResultBuilder = ImmutableSet.builder();
    }

    public ImmutableSet<GtmTagOperationResult> execute() throws IOException {
      BatchRequest batchRequest = tagManagerService.batch();

      gtmExport
          .getFloodlightActivities()
          .forEach(
              floodlightActivity ->
                  addInsertFloodlightOperationToBatch(floodlightActivity, batchRequest));

      batchRequest.execute();

      return tagOperationResultBuilder.build();
    }

    private void addInsertFloodlightOperationToBatch(
        GtmFloodlightActivity floodlightActivity, BatchRequest batchRequest) {

      /** Update the status of operation result based on bulk callback response. */
      class GtmTagAddOperationCallback extends JsonBatchCallback<Tag> {

        @Override
        public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
          tagOperationResultBuilder.add(
              GtmTagOperationResult.newBuilder()
                  .setFloodlightActivity(floodlightActivity.getName())
                  .setSuccess(false)
                  .setMessage(googleJsonError.getMessage())
                  .build());
          logger.atWarning().log("error adding floodlightActivity:\n %s", googleJsonError);
        }

        @Override
        public void onSuccess(Tag aVoid, HttpHeaders httpHeaders) {
          tagOperationResultBuilder.add(
              GtmTagOperationResult.newBuilder()
                  .setFloodlightActivity(floodlightActivity.getName())
                  .setSuccess(true)
                  .build());
        }
      }

      try {
        tagManagerService
            .accounts()
            .containers()
            .tags()
            .create(
                gtmContainer.getAccountId(),
                gtmContainer.getContainerId(),
                GtmTagTransformer.transform(floodlightActivity, gtmExport.getId()))
            .queue(batchRequest, new GtmTagAddOperationCallback());
      } catch (IOException ioexception) {
        tagOperationResultBuilder.add(
            GtmTagOperationResult.newBuilder()
                .setSuccess(false)
                .setFloodlightActivity("floodlightActivity")
                .setMessage(ioexception.getMessage())
                .build());
        logger.atWarning().withCause(ioexception).withStackTrace(StackSize.MEDIUM).log(
            "Failed Adding Tag to batch insert: %s", floodlightActivity);
      }
    }
  }
}

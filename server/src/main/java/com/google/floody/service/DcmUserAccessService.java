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
import static com.google.floody.model.DcmObjects.createAccountDcmObject;
import static com.google.floody.model.DcmObjects.createAdvertiserDcmObject;
import static com.google.floody.model.DcmObjects.createUserProfileDcmObject;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.model.AccountsListResponse;
import com.google.api.services.dfareporting.model.AdvertisersListResponse;
import com.google.api.services.dfareporting.model.UserProfile;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.protobuf.UserOperations.DcmObject.DcmObjectType;
import com.google.floody.protobuf.UserOperations.DcmObjectList;
import java.io.IOException;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A DCM Micro-Service provides methods to retrieve user access details to 'DCM Profiles, Network &
 * Floodlight Configurations'.
 */
public final class DcmUserAccessService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Dfareporting dcmService;

  public DcmUserAccessService(Dfareporting dcmService) {
    this.dcmService = dcmService;
  }

  /**
   * Returns user profiles of the logged in user for the given network. Returns all user-profiles if
   * no accountId provided.
   *
   * @param accountId optional - the account/network id to filter user profiles for the given user.
   */
  public DcmObjectList listUserProfilesForAccount(@Nullable Long accountId) {
    try {
      var profiles =
          Optional.ofNullable(dcmService.userProfiles().list().execute().getItems())
              .orElse(emptyList())
              .stream()
              .filter(item -> (accountId == null || item.getAccountId().equals(accountId)))
              .map(
                  userProfile ->
                      createUserProfileDcmObject(
                          userProfile.getProfileId(), formatUserProfileName(userProfile)))
              .collect(toImmutableSet());

      return DcmObjectList.newBuilder().addAllItems(profiles).build();
    } catch (IOException ioexp) {
      logger.atSevere().withCause(ioexp).log(
          "Error retrieving User Profiles connected with the user with networkId (%s)", accountId);
    }

    return DcmObjectList.getDefaultInstance();
  }

  /**
   * Returns all DCM Networks accessible to the logged in user, the output is flattened from the
   * paginated input by using nextPage Token to make multiple calls to the DCM API.
   *
   * @param profileId selected DCM Profile Id of the user
   * @return a list of all Network Id and Names as {@link DcmObjectType}.ACCOUNT or empty list if
   *     there was an error in retrieval
   */
  public DcmObjectList getAllAccessibleAccounts(long profileId) {

    DcmObjectList.Builder accessibleAccountListBuilder = DcmObjectList.newBuilder();

    try {
      String nextPageToken = null;

      do {
        AccountsListResponse response =
            dcmService.accounts().list(profileId).setPageToken(nextPageToken).execute();
        nextPageToken = response.getNextPageToken();
        response
            .getAccounts()
            .forEach(
                account ->
                    accessibleAccountListBuilder.addItems(
                        createAccountDcmObject(account.getId(), account.getName())));
      } while (nonNull(nextPageToken));
    } catch (IOException ioexp) {
      logger.atWarning().withCause(ioexp).log(
          "Error in retrieving Accessible Accounts for profileId - %d", profileId);
    }

    return accessibleAccountListBuilder.build();
  }

  /**
   * Returns all DCM Floodlight Configurations accessible to the logged in user, the output is
   * flattened by using nextPage token to make multiple calls to the DCM API.
   *
   * @param profileId selected DCM Profile Id of the user
   * @param accountId DCM AccountId to filter returned Floodlight Configurations.
   * @return a list of all DCM Floodlight Configuration Ids & Names accessible to the user or empty
   *     list in case of an error
   */
  public DcmObjectList getAllAccessibleFloodlightConfigs(long profileId, Long accountId) {
    DcmObjectList.Builder floodlightConfigBuilder = DcmObjectList.newBuilder();

    try {
      String nextPageToken = null;

      // Get All Advertiser Names
      do {
        AdvertisersListResponse response =
            dcmService
                .advertisers()
                .list(profileId)
                .setOnlyParent(true) // retrieve only master floodlight configurations
                .setPageToken(nextPageToken)
                .execute();
        nextPageToken = response.getNextPageToken();

        response.getAdvertisers().stream()
            .filter(advertiser -> accountId == null || advertiser.getAccountId().equals(accountId))
            .forEach(
                advertiser ->
                    floodlightConfigBuilder.addItems(
                        createAdvertiserDcmObject(advertiser.getId(), advertiser.getName())));
      } while (nonNull(nextPageToken));
    } catch (IOException ioexp) {
      logger.atWarning().withCause(ioexp).log(
          "Error in retrieving Floodlight Configurations for profileId - %d", profileId);
    }

    return floodlightConfigBuilder.build();
  }

  /**
   * Formats the Name to contain DCM Network Id in parenthesis.
   *
   * @param userProfile the DCM user profile to format.
   */
  private static String formatUserProfileName(UserProfile userProfile) {
    return "(" + userProfile.getAccountId() + ") " + userProfile.getUserName();
  }
}

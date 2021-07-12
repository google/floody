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

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.common.collect.ImmutableSet;
import com.google.floody.auth.CredentialService;
import java.io.IOException;

/** Utility Service to fetch information of Google issued OAuth2 Access tokens. */
public final class OAuth2TokenInfoService {

  private final CredentialService credentialService;
  private final String applicationName;

  public OAuth2TokenInfoService(CredentialService credentialService, String applicationName) {
    this.credentialService = credentialService;
    this.applicationName = applicationName;
  }

  /**
   * Returns the information of the Access-Token by retrieving it from Google's OAuth Service.
   *
   * @throws IOException if the token is invalid or expired.
   */
  public Tokeninfo retrieveTokenInfo() throws IOException {
    return new Oauth2.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(ImmutableSet.of()))
        .setApplicationName(applicationName)
        .build()
        .tokeninfo()
        .execute();
  }

  public String retrieveEmailAddress() throws IOException {
    return retrieveTokenInfo().getEmail();
  }
}

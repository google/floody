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

package com.google.floody.auth;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;

/** Utility class to provide Google Credential creation methods */
public final class AccessTokenCredentialService implements CredentialService {

  private final String accessToken;

  public AccessTokenCredentialService(String accessToken) {
    checkArgument(!isBlank(accessToken), "Access token is null or empty");
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public Credentials getCredentialForScope(String scope) throws IOException {
    return getCredentialForScopes(ImmutableList.of(scope));
  }

  @Override
  public Credentials getCredentialForScopes(Collection<String> scopes) throws IOException {
    return GoogleCredentials.create(new AccessToken(accessToken, null));
  }

  @Override
  public HttpCredentialsAdapter getClientInitializerForScope(String scope) throws IOException {
    return getClientInitializerForScope(ImmutableSet.of(scope));
  }

  @Override
  public HttpCredentialsAdapter getClientInitializerForScope(Collection<String> scopes)
      throws IOException {
    return new HttpCredentialsAdapter(getCredentialForScopes(scopes));
  }
}

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

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;

/**
 * Concrete implementation of {@link CredentialService} to build {@link GoogleCredentials} objects
 * for the default service account
 */
public final class SystemAccountCredentialService implements CredentialService {

  @Override
  public final GoogleCredentials getCredentialForScope(String scope) throws IOException {
    return getCredentialForScopes(ImmutableSet.of(scope));
  }

  @Override
  public final GoogleCredentials getCredentialForScopes(Collection<String> scopes)
      throws IOException {
    return GoogleCredentials.getApplicationDefault()
        .createScoped(isEmpty(scopes) ? ImmutableSet.of("email") : scopes);
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

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

import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import java.io.IOException;
import java.util.Collection;

/**
 * Methods to abstract building OAuth Credentials for different systems including ones for Google,
 * e.g. {@code GoogleAppEngineDefaultServiceAccountCredentialService}
 */
public interface CredentialService {

  /**
   * Builds an OAuth Credential for the given Single Scope.
   *
   * @param scope the required OAuth authorization scope
   * @return A qualified OAuth credential for the given scope
   */
  Credentials getCredentialForScope(String scope) throws IOException;

  /**
   * Builds an OAuth Credential for multiple scopes as listed through {@code scopes}
   *
   * @param scopes a list of required authorization scopes
   * @return A qualified credential for the given list of scopes
   */
  Credentials getCredentialForScopes(Collection<String> scopes) throws IOException;

  HttpCredentialsAdapter getClientInitializerForScope(String scope) throws IOException;

  HttpCredentialsAdapter getClientInitializerForScope(Collection<String> scopes) throws IOException;
}

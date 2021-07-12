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

/**
 * Factory pattern to instantiate concrete implementations of {@link CredentialService} e.g {@code
 * GoogleAppEngineDefaultServiceAccountCredentialService}
 */
public final class CredentialServiceFactory {

  /**
   * Provides a factory method to instantiate {@link SystemAccountCredentialService} for building
   * Appengine Service account credentials
   *
   * @return the instance to build credentials for the AppEngine's default Service Account
   */
  public static SystemAccountCredentialService buildSystemCredentialService() {
    return new SystemAccountCredentialService();
  }
}

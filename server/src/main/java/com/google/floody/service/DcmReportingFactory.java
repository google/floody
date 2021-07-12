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

import static com.google.api.services.dfareporting.DfareportingScopes.DFATRAFFICKING;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.dfareporting.Dfareporting;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.auth.CredentialService;
import java.io.IOException;

/**
 * Factory class to build instances of {@link Dfareporting} providing DCM API client to be used in
 * different services.
 */
public final class DcmReportingFactory {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final CredentialService credentialService;

  private final String applicationName;

  /**
   * @param credentialService the {@link Credential} Object to use for making API calls
   * @param applicationName the name of the client application
   */
  public DcmReportingFactory(CredentialService credentialService, String applicationName) {
    this.credentialService = credentialService;
    this.applicationName = applicationName;
  }

  /**
   * Builds a DCM API Client based on provided accessToken.
   *
   * @return DCM API Client for use
   */
  public Dfareporting buildDcmService() throws IOException {
    logger.atFine().log("DcmService Build Request");

    return new Dfareporting.Builder(
            new NetHttpTransport(),
            new GsonFactory(),
            credentialService.getClientInitializerForScope(DFATRAFFICKING))
        .setApplicationName(applicationName)
        .build();
  }
}

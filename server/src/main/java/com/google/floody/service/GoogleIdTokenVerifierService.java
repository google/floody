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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Optional;

/** Service to verify Google issued OIDC tokens. */
public final class GoogleIdTokenVerifierService {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final String idToken;

  public GoogleIdTokenVerifierService(String idToken) {
    this.idToken = idToken;
  }

  /**
   * Returns the GoogleTokenPayload if the OIDC token is valid.
   *
   * @param audiences the audiences of the OIDC token or an empty collection.
   * @throws GeneralSecurityException if the OIDC signature doesn't match.
   * @throws IOException when there is error accessing the network for Google Public Key.
   */
  public Optional<Payload> getIdTokenPayload(Collection<String> audiences)
      throws GeneralSecurityException, IOException {

    var builder = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory());

    if (!audiences.isEmpty()) {
      builder.setAudience(audiences);
    }
    return Optional.ofNullable(builder.build().verify(idToken)).map(GoogleIdToken::getPayload);
  }

  /** Returns the <b>verified</b> email address for OIDC or empty if the email is not verified. */
  public Optional<String> retrieveEmailAddress() {
    try {
      return getIdTokenPayload(ImmutableList.of())
          .filter(Payload::getEmailVerified)
          .map(Payload::getEmail);
    } catch (IOException | GeneralSecurityException ex) {
      logger.atWarning().log("Error verifying IdToken (%s) ", ex.getMessage());
    }

    return Optional.empty();
  }
}

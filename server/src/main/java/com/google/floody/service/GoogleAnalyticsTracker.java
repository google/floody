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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.protobuf.Analytics.AnalyticsHitData;
import com.google.floody.protobuf.Analytics.IdType;
import java.io.IOException;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;

/** Service to send a hit to GA servers by creating an HTTP Request based on hit data. */
public final class GoogleAnalyticsTracker {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final String gaPropertyId;
  private final ImmutableList<String> logsPiiAttributes;
  private final boolean debugLogging;

  public GoogleAnalyticsTracker(
      String gaPropertyId, List<String> logsPiiAttributes, boolean debugLogging) {
    this.gaPropertyId = gaPropertyId;
    this.logsPiiAttributes =
        (logsPiiAttributes != null) ? ImmutableList.copyOf(logsPiiAttributes) : ImmutableList.of();
    this.debugLogging = debugLogging;
  }

  public void postToAnalyticsServer(AnalyticsHitData hitData) {
    HttpUrl analyticsHitUrl = new HitPayloadTransformer(hitData).buildHitUrl();
    try (Response response = sendPayloadToGaServer(analyticsHitUrl)) {
      logger.atInfo().log(
          "send hit (response: %s) hit:\n%s",
          response.code(), redactSensitiveQueryParamValues(analyticsHitUrl));
    } catch (IOException ioexp) {
      logger.atSevere().withCause(ioexp).log("Analytics hit sending failed\n%s", hitData);
    }
  }

  private static Response sendPayloadToGaServer(HttpUrl filledAnalyticsUrl) throws IOException {
    return new OkHttpClient()
        .newCall(new Request.Builder().url(filledAnalyticsUrl).build())
        .execute();
  }

  private ImmutableMap<String, String> redactSensitiveQueryParamValues(HttpUrl hitUrl) {
    return hitUrl.queryParameterNames().stream()
        .map(
            paramName ->
                ImmutablePair.of(
                    paramName,
                    !debugLogging && logsPiiAttributes.contains(paramName)
                        ? "REDACTED"
                        : hitUrl.queryParameterValues(paramName).get(0)))
        .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));
  }

  /**
   * Returns a Measurement Protocol message as per guidance
   * https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters
   */
  private class HitPayloadTransformer {

    private final AnalyticsHitData hitData;

    public HitPayloadTransformer(AnalyticsHitData hitData) {
      this.hitData = hitData;
    }

    private HttpUrl buildHitUrl() {
      HttpUrl.Builder urlBuilder =
          new HttpUrl.Builder()
              .scheme(Server.SCHEME)
              .host(Server.HOST)
              .encodedPath(Server.PATH)
              .addQueryParameter("v", "1")
              .addQueryParameter("t", "event")
              .addQueryParameter("tid", firstNonNull(hitData.getPropertyId(), gaPropertyId))
              .addQueryParameter("ds", hitData.getUserInformation().getEventSource().name())
              .addQueryParameter(extractUserIdType(), hitData.getUserInformation().getId())
              .addQueryParameter("ec", hitData.getEventInformation().getEventCategory().name())
              .addQueryParameter("ea", hitData.getEventInformation().getEventAction().name());

      if (hitData.getUserIp() != null) {
        urlBuilder.addQueryParameter("uip", hitData.getUserIp());
      }

      if (hitData.getDocumentLocation() != null) {
        urlBuilder.addQueryParameter("dl", hitData.getDocumentLocation());
      }

      if (hitData.getUserAgent() != null) {
        urlBuilder.addQueryParameter("ua", hitData.getUserAgent());
      }

      return urlBuilder.build();
    }

    private String extractUserIdType() {
      return hitData.getUserInformation().getIdType().equals(IdType.GAID) ? "cid" : "uid";
    }
  }

  public static final class Server {

    public static final String HOST = "www.google-analytics.com";
    public static final String PATH = "/collect";
    public static final String SCHEME = "https";

    private Server() {}
  }
}

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

package com.google.floody.transforms;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.floody.protobuf.Analytics.AnalyticsHitData;
import com.google.floody.protobuf.Analytics.EventAction;
import com.google.floody.protobuf.Analytics.EventCategory;
import com.google.floody.protobuf.Analytics.EventInformation;
import com.google.floody.protobuf.Analytics.EventSource;
import com.google.floody.protobuf.Analytics.IdType;
import com.google.floody.protobuf.Analytics.UserInformation;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** Helper to build a Google Analytics Hits payload by inspecting the request. */
@AutoValue
public abstract class AnalyticsHitDataExtractor {

  abstract HttpServletRequest request();

  abstract String gaPropertyId();

  public AnalyticsHitData extract() {
    return AnalyticsHitData.newBuilder()
        .setPropertyId(gaPropertyId())
        .setUserIp(extractUserIp())
        .setUserAgent(extractUserAgent())
        .setDocumentLocation(extractRequestUrl())
        .setUserInformation(extractUserInformation())
        .setEventInformation(extractEventInformation())
        .build();
  }

  private String extractRequestUrl() {
    StringBuffer requestURL = request().getRequestURL();
    if (request().getQueryString() != null) {
      requestURL.append("?").append(request().getQueryString());
    }
    return requestURL.toString();
  }

  private String extractUserAgent() {
    return request().getHeader("User-Agent");
  }

  private String extractUserIp() {
    // TODO: Update with Cloud Run user IP.
    String xAppEngineUserIp = request().getHeader("X-AppEngine-User-IP");
    String xAppEngineUserIpLower = request().getHeader("x-appengine-user-ip");
    String xForwardedFor = request().getHeader("X-Forwarded-For");

    if (xAppEngineUserIp != null) {
      return xAppEngineUserIp;
    } else if (xAppEngineUserIpLower != null) {
      return xAppEngineUserIpLower;
    } else if (xForwardedFor != null) {
      return xForwardedFor;
    }
    return request().getRemoteAddr();
  }

  private UserInformation extractUserInformation() {
    Optional<String> gaUid = extractGaCookieUserId();

    if (gaUid.isPresent()) {
      return UserInformation.newBuilder()
          .setEventSource(EventSource.WEB)
          .setIdType(IdType.GAID)
          .setId(gaUid.get())
          .build();
    }

    return UserInformation.getDefaultInstance();
  }

  private Optional<String> extractGaCookieUserId() {
    Cookie[] cookies = request().getCookies();

    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals("_ga"))
        .findFirst()
        .map(Cookie::getValue)
        // remove GA cookie Version information.
        .map(rawGaCookie -> rawGaCookie.replaceFirst("GA\\d+\\.\\d+\\.", ""));
  }

  private EventInformation extractEventInformation() {
    String uri = request().getRequestURI();

    return URL_ACTION_MAP.entrySet().stream()
        .filter(entry -> uri.startsWith(entry.getKey()))
        .findFirst()
        .map(entry -> entry.getValue())
        .orElse(EventInformation.getDefaultInstance());
  }

  public static Builder builder() {
    return new AutoValue_AnalyticsHitDataExtractor.Builder();
  }

  /** Convenience builder for Extractor. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setRequest(HttpServletRequest httpServletRequest);

    public abstract Builder setGaPropertyId(String gaPropertyId);

    public abstract AnalyticsHitDataExtractor build();
  }

  /** Mapping with URI to user action to use for sending GA hit. */
  private static final ImmutableMap<String, EventInformation> URL_ACTION_MAP =
      new ImmutableMap.Builder<String, EventInformation>()
          .put(
              "/admin/init",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.SHEET_OPS)
                  .setEventAction(EventAction.CREATE_SHEET)
                  .build())
          .put(
              "/admin/addRows",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.SHEET_OPS)
                  .setEventAction(EventAction.ADD_ROWS)
                  .build())
          .put(
              "/admin/updateTitle",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.SHEET_OPS)
                  .setEventAction(EventAction.UPDATE_SPREADSHEET_TITLE)
                  .build())
          .put(
              "/admin/share",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.SHEET_OPS)
                  .setEventAction(EventAction.SHARE_SHEET)
                  .build())
          .put(
              "/floody/exportToSheet",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.FLOODY_OPS)
                  .setEventAction(EventAction.IMPORT_FROM_DCM)
                  .build())
          .put(
              "/floody/exportToDcm",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.FLOODY_OPS)
                  .setEventAction(EventAction.EXPORT_TO_DCM)
                  .build())
          .put(
              "/user/recentSheets",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.USER_OPS)
                  .setEventAction(EventAction.LIST_SHEETS)
                  .build())
          .put(
              "/user/checkUserAuth",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.USER_OPS)
                  .setEventAction(EventAction.SPREADSHEET_AUTH_CHECK)
                  .build())
          .put(
              "/user/floodlightconfigs",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.USER_OPS)
                  .setEventAction(EventAction.LIST_ACCESSIBLE_FLOODLIGHT_CONFIGS)
                  .build())
          .put(
              "/user/accounts",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.USER_OPS)
                  .setEventAction(EventAction.LIST_ACCESSIBLE_ACCOUNTS)
                  .build())
          .put(
              "/user/profiles",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.USER_OPS)
                  .setEventAction(EventAction.LIST_USER_PROFILES)
                  .build())
          .put(
              "/gtm/request/spreadsheet",
              EventInformation.newBuilder()
                  .setEventCategory(EventCategory.GTM)
                  .setEventAction(EventAction.EXPORT_TO_GTM_REQUEST)
                  .build())
          .build();
}

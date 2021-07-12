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
package com.google.floody.model;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "floody")
@ConstructorBinding
public class FloodyProperties {

  private final String clientId;
  private final String executionEnvironment;
  private final String applicationName;
  private final String sheetMetadataFloodlightConfigurationIdKey;
  private final String sheetMetadataAccountIdKey;
  private final Integer generatedFileTtlDays;
  private final Integer defaultAudienceMembershipDurationDays;
  private final List<String> adminGroupEmails;
  private final String analyticsPropertyId;
  private final List<String> logsPiiAttributes;
  private final List<String> analyticsExcludedUris;
  private final String logPiiQueryParam;

  public FloodyProperties(
      String clientId,
      String executionEnvironment,
      String applicationName,
      String sheetMetadataFloodlightConfigurationIdKey,
      String sheetMetadataAccountIdKey,
      Integer generatedFileTtlDays,
      Integer defaultAudienceMembershipDurationDays,
      List<String> adminGroupEmails,
      String analyticsPropertyId,
      List<String> logsPiiAttributes,
      List<String> analyticsExcludedUris,
      String logPiiQueryParam) {
    this.clientId = clientId;
    this.executionEnvironment = executionEnvironment;
    this.applicationName = applicationName;
    this.sheetMetadataFloodlightConfigurationIdKey = sheetMetadataFloodlightConfigurationIdKey;
    this.sheetMetadataAccountIdKey = sheetMetadataAccountIdKey;
    this.generatedFileTtlDays = generatedFileTtlDays;
    this.defaultAudienceMembershipDurationDays = defaultAudienceMembershipDurationDays;
    this.adminGroupEmails = adminGroupEmails;
    this.analyticsPropertyId = analyticsPropertyId;
    this.logsPiiAttributes = logsPiiAttributes;
    this.analyticsExcludedUris = analyticsExcludedUris;
    this.logPiiQueryParam = logPiiQueryParam;
  }

  public String getClientId() {
    return clientId;
  }

  public String getExecutionEnvironment() {
    return executionEnvironment;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getSheetMetadataFloodlightConfigurationIdKey() {
    return sheetMetadataFloodlightConfigurationIdKey;
  }

  public String getSheetMetadataAccountIdKey() {
    return sheetMetadataAccountIdKey;
  }

  public Integer getGeneratedFileTtlDays() {
    return generatedFileTtlDays;
  }

  public Integer getDefaultAudienceMembershipDurationDays() {
    return defaultAudienceMembershipDurationDays;
  }

  public List<String> getAdminGroupEmails() {
    return adminGroupEmails;
  }

  public String getAnalyticsPropertyId() {
    return analyticsPropertyId;
  }

  public List<String> getLogsPiiAttributes() {
    return logsPiiAttributes;
  }

  public List<String> getAnalyticsExcludedUris() {
    return analyticsExcludedUris;
  }

  public String getLogPiiQueryParam() {
    return logPiiQueryParam;
  }
}

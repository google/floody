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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.floody.model.GtmRequestAction.Action;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Entity
@JsonDeserialize(builder = GtmExport.Builder.class)
@JsonInclude(Include.NON_NULL)
public final class GtmExport {

  @Id private Long id;

  private String gtmContainerId;

  private String requesterEmail;

  private String spreadsheetId;

  @Index private Set<String> approverEmails;

  private Set<GtmFloodlightActivity> floodlightActivities;

  @Nullable private String requesterMessage;

  @Index private DateTime timestamp;

  @Nullable private GtmRequestAction actionInformation;

  @Nullable private Set<TagOperationResult> tagOperationResults;

  public GtmExport(
      Long id,
      String gtmContainerId,
      String requesterEmail,
      String spreadsheetId,
      String requesterMessage,
      Set<String> approverEmails,
      Set<GtmFloodlightActivity> floodlightActivities,
      DateTime timestamp,
      GtmRequestAction actionInformation,
      Set<TagOperationResult> tagOperationResults) {
    checkArgument(isNotBlank(spreadsheetId), "spreadsheetId should not be blank");

    this.id = id;
    this.gtmContainerId = gtmContainerId;
    this.requesterEmail = requesterEmail;
    this.spreadsheetId = spreadsheetId;
    this.requesterMessage = requesterMessage;
    this.approverEmails = approverEmails;
    this.floodlightActivities = checkNotNull(floodlightActivities);
    this.timestamp = checkNotNull(timestamp);
    this.actionInformation = actionInformation;
    this.tagOperationResults = tagOperationResults;
  }

  /** Default Constructor required for Objectify. */
  private GtmExport() {}

  public GtmExport withApprovalNow(String authorizer, String comment) {
    return toBuilder()
        .setActionInformation(GtmRequestAction.now(Action.APPROVED, authorizer, comment))
        .build();
  }

  public GtmExport withRejectionNow(String authorizer, String comment) {
    return toBuilder()
        .setActionInformation(GtmRequestAction.now(Action.REJECTED, authorizer, comment))
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GtmExport)) {
      return false;
    }

    GtmExport that = (GtmExport) o;
    return Objects.equals(id, that.id)
        && Objects.equals(gtmContainerId, that.gtmContainerId)
        && Objects.equals(requesterEmail, that.requesterEmail)
        && Objects.equals(requesterMessage, that.requesterMessage)
        && Objects.equals(approverEmails, that.approverEmails)
        && Objects.equals(floodlightActivities, that.floodlightActivities)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(actionInformation, that.actionInformation)
        && Objects.equals(tagOperationResults, that.tagOperationResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        gtmContainerId,
        requesterEmail,
        requesterMessage,
        approverEmails,
        floodlightActivities,
        timestamp,
        actionInformation,
        tagOperationResults);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("gtmContainerId", gtmContainerId)
        .add("requesterEmail", requesterEmail)
        .add("spreadsheetId", spreadsheetId)
        .add("requesterMessage", requesterMessage)
        .add("approverEmails", approverEmails)
        .add("floodlightActivities", floodlightActivities)
        .add("timestamp", timestamp)
        .add("actionInformation", actionInformation)
        .add("gtmTagOperationResults", tagOperationResults)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public GtmExport withRequesterEmail(String requesterEmail) {
    return this.toBuilder().setRequesterEmail(requesterEmail).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {

    private Long id;
    private String gtmContainerId;
    private String requesterEmail;
    private String requesterMessage;
    private String spreadsheetId;
    private Set<String> approverEmails;
    private Set<GtmFloodlightActivity> floodlightActivities;
    private DateTime timestamp;
    private GtmRequestAction actionInformation;
    private Set<TagOperationResult> tagOperationResults;

    public Builder() {}

    public Builder(GtmExport request) {
      this.id = request.id;
      this.gtmContainerId = request.gtmContainerId;
      this.requesterEmail = request.requesterEmail;
      this.spreadsheetId = request.spreadsheetId;
      this.requesterMessage = request.requesterMessage;
      this.approverEmails = request.approverEmails;
      this.floodlightActivities = request.floodlightActivities;
      this.timestamp = request.timestamp;
      this.actionInformation = request.actionInformation;
      this.tagOperationResults = request.tagOperationResults;
    }

    public Builder setId(Long id) {
      this.id = id;
      return this;
    }

    public Builder setGtmContainerId(String gtmContainerId) {
      this.gtmContainerId = gtmContainerId;
      return this;
    }

    public Builder setRequesterEmail(String requesterEmail) {
      this.requesterEmail = requesterEmail;
      return this;
    }

    public Builder setSpreadsheetId(String spreadsheetId) {
      this.spreadsheetId = spreadsheetId;
      return this;
    }

    public Builder setRequesterMessage(@Nullable String requesterMessage) {
      this.requesterMessage = requesterMessage;
      return this;
    }

    public Builder setApproverEmails(Collection<String> approverEmails) {
      this.approverEmails = ImmutableSet.copyOf(approverEmails);
      return this;
    }

    public Builder setFloodlightActivities(
        @Nullable Collection<GtmFloodlightActivity> floodlightActivities) {
      checkNotNull(floodlightActivities);
      this.floodlightActivities = ImmutableSet.copyOf(floodlightActivities);
      return this;
    }

    public Builder setTimestamp(DateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder setTimestamp(String timestamp) {
      this.timestamp = DateTime.parse(timestamp);
      return this;
    }

    public Builder setActionInformation(@Nullable GtmRequestAction actionInformation) {
      this.actionInformation = actionInformation;
      return this;
    }

    public Builder setGtmTagOperationResults(
        @Nullable Collection<TagOperationResult> tagOperationResults) {
      if (tagOperationResults != null) {
        this.tagOperationResults = Set.copyOf(tagOperationResults);
      } else {
        this.tagOperationResults = null;
      }
      return this;
    }

    public GtmExport build() {
      return new GtmExport(
          id,
          gtmContainerId,
          requesterEmail,
          spreadsheetId,
          requesterMessage,
          approverEmails,
          floodlightActivities,
          firstNonNull(timestamp, DateTime.now(DateTimeZone.UTC)),
          actionInformation,
          tagOperationResults);
    }
  }

  public Long getId() {
    return id;
  }

  public String getGtmContainerId() {
    return gtmContainerId;
  }

  public String getRequesterEmail() {
    return requesterEmail;
  }

  public String getSpreadsheetId() {
    return spreadsheetId;
  }

  @Nullable
  public String getRequesterMessage() {
    return requesterMessage;
  }

  public Set<String> getApproverEmails() {
    return unmodifiableSet(approverEmails);
  }

  public Set<GtmFloodlightActivity> getFloodlightActivities() {
    return unmodifiableSet(floodlightActivities);
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  @Nullable
  public GtmRequestAction getActionInformation() {
    return actionInformation;
  }

  @Nullable
  public Set<TagOperationResult> getGtmTagOperationResults() {
    return tagOperationResults;
  }
}

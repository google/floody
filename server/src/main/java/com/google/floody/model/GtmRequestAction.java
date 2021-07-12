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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class GtmRequestAction {

  public enum Action {
    APPROVED,
    REJECTED
  }

  private DateTime timestamp;
  private String authorizer;
  private Action action;
  private String comment;

  private GtmRequestAction() {}

  private GtmRequestAction(Action action, DateTime timestamp, String authorizer, String comment) {
    this.action = checkNotNull(action);
    this.timestamp = checkNotNull(timestamp);
    this.authorizer = checkNotNull(authorizer);
    this.comment = comment;
  }

  public static GtmRequestAction now(Action action, String authorizer, String comment) {
    return builder().setAction(action).setAuthorizer(authorizer).setComment(comment).now().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  public String getAuthorizer() {
    return authorizer;
  }

  public Action getAction() {
    return action;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GtmRequestAction)) {
      return false;
    }
    GtmRequestAction that = (GtmRequestAction) o;
    return Objects.equals(timestamp, that.timestamp)
        && Objects.equals(authorizer, that.authorizer)
        && action == that.action
        && Objects.equals(comment, that.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, authorizer, action, comment);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestamp", timestamp)
        .add("authorizer", authorizer)
        .add("action", action)
        .add("comment", comment)
        .toString();
  }

  public static class Builder {

    private DateTime timestamp;
    private String authorizer;
    private Action action;
    private String comment;

    public Builder setTimestamp(DateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder now() {
      return setTimestamp(DateTime.now(DateTimeZone.UTC));
    }

    public Builder setAuthorizer(String authorizer) {
      this.authorizer = authorizer;
      return this;
    }

    public Builder setAction(Action action) {
      this.action = action;
      return this;
    }

    public Builder setComment(String comment) {
      this.comment = comment;
      return this;
    }

    public GtmRequestAction build() {
      return new GtmRequestAction(action, timestamp, authorizer, comment);
    }
  }
}

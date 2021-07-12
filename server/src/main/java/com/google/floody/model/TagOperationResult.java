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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = TagOperationResult.Builder.class)
public final class TagOperationResult {

  private String floodlightActivityName;
  private Boolean success;
  private String message;

  private TagOperationResult(String floodlightActivityName, Boolean success, String message) {
    this.floodlightActivityName = floodlightActivityName;
    this.success = success;
    this.message = message;
  }

  private TagOperationResult() {}

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("floodlightActivityName", floodlightActivityName)
        .add("success", success)
        .add("message", message)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {

    private String floodlightActivityName;
    private Boolean success;
    private String message;

    public Builder() {}

    public Builder(TagOperationResult result) {
      this.floodlightActivityName = result.floodlightActivityName;
      this.success = result.success;
      this.message = result.message;
    }

    public TagOperationResult.Builder setFloodlightActivityName(String floodlightActivityName) {
      this.floodlightActivityName = floodlightActivityName;
      return this;
    }

    public TagOperationResult.Builder setSuccess(Boolean success) {
      this.success = success;
      return this;
    }

    public TagOperationResult.Builder setMessage(String message) {
      this.message = message;
      return this;
    }

    public TagOperationResult build() {
      return new TagOperationResult(floodlightActivityName, success, message);
    }
  }

  public String getFloodlightActivityName() {
    return floodlightActivityName;
  }

  public Boolean getSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }
}

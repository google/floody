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
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = GtmFloodlightActivity.Builder.class)
public final class GtmFloodlightActivity {

  private String name;
  private Long dcmAdvertiserId;
  private String type;
  private String cat;
  private FloodyCountingMethod countingMethod;

  @Nullable private Set<String> customVariables;

  private GtmFloodlightActivity(
      String name,
      Long dcmAdvertiserId,
      String type,
      String cat,
      FloodyCountingMethod countingMethod,
      @Nullable Set<String> customVariables) {
    this.name = name;
    this.dcmAdvertiserId = dcmAdvertiserId;
    this.type = type;
    this.cat = cat;
    this.countingMethod = countingMethod;
    this.customVariables = customVariables;
  }

  /** Default Constructor required for Objectify. */
  private GtmFloodlightActivity() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GtmFloodlightActivity)) {
      return false;
    }
    GtmFloodlightActivity that = (GtmFloodlightActivity) o;
    return Objects.equals(name, that.name)
        && Objects.equals(dcmAdvertiserId, that.dcmAdvertiserId)
        && Objects.equals(type, that.type)
        && Objects.equals(cat, that.cat)
        && Objects.equals(countingMethod, that.countingMethod)
        && Objects.equals(customVariables, that.customVariables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dcmAdvertiserId, type, cat, countingMethod, customVariables);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("dcmAdvertiserId", dcmAdvertiserId)
        .add("type", type)
        .add("cat", cat)
        .add("countingMethod", countingMethod)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder().setCustomVariables(ImmutableSet.of());
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {

    private String name;
    private Long dcmAdvertiserId;
    private String type;
    private String cat;
    private FloodyCountingMethod countingMethod;
    private Set<String> customVariables;

    public Builder() {}

    public Builder(GtmFloodlightActivity activity) {
      this.name = activity.name;
      this.dcmAdvertiserId = activity.dcmAdvertiserId;
      this.type = activity.type;
      this.cat = activity.cat;
      this.countingMethod = activity.countingMethod;
      this.customVariables = activity.customVariables;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setDcmAdvertiserId(Long dcmAdvertiserId) {
      this.dcmAdvertiserId = dcmAdvertiserId;
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setCat(String cat) {
      this.cat = cat;
      return this;
    }

    public Builder setCountingMethod(FloodyCountingMethod countingMethod) {
      this.countingMethod = countingMethod;
      return this;
    }

    public Builder setCustomVariables(Collection<String> customVariables) {
      if (customVariables != null) {
        this.customVariables = ImmutableSet.copyOf(customVariables);
      }

      return this;
    }

    public GtmFloodlightActivity build() {
      return new GtmFloodlightActivity(
          name, dcmAdvertiserId, type, cat, countingMethod, customVariables);
    }
  }

  public String getName() {
    return name;
  }

  public Long getDcmAdvertiserId() {
    return dcmAdvertiserId;
  }

  public String getType() {
    return type;
  }

  public String getCat() {
    return cat;
  }

  public FloodyCountingMethod getCountingMethod() {
    return countingMethod;
  }

  @Nullable
  public Set<String> getCustomVariables() {
    return (customVariables == null) ? null : Collections.unmodifiableSet(customVariables);
  }
}

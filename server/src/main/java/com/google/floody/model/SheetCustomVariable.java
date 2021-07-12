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

import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Floody's representation of {@code UserDefinedVariableConfiguration} based on DCM API to easily
 * manage Custom Variables as defined in Floodlight Configuration of the Advertiser.
 */
@AutoValue
public abstract class SheetCustomVariable {

  public abstract String getNumber();

  public abstract String getType();

  @Nullable
  public abstract String getName();

  public static Builder builder() {
    return new AutoValue_SheetCustomVariable.Builder();
  }

  /** AutoValue builder class to help instantiate SheetCustomVariable */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setNumber(String number);

    public abstract Builder setType(String name);

    public abstract Builder setName(@Nullable String reportName);

    public abstract SheetCustomVariable build();
  }
}

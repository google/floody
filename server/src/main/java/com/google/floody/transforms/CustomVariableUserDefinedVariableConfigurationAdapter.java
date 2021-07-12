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

import com.google.api.services.dfareporting.model.UserDefinedVariableConfiguration;
import com.google.floody.model.SheetCustomVariable;

/**
 * Adapter class to transform {@link UserDefinedVariableConfiguration} to {@link
 * SheetCustomVariable} and reverse.
 */
public final class CustomVariableUserDefinedVariableConfigurationAdapter {

  private CustomVariableUserDefinedVariableConfigurationAdapter() {}

  /**
   * Adapter pattern to convert {@link UserDefinedVariableConfiguration} to {@link
   * SheetCustomVariable} by remapping fields.
   *
   * @param config UserDefined variable configuration
   * @return the Custom variable with mapped values
   */
  public static SheetCustomVariable transform(UserDefinedVariableConfiguration config) {
    return SheetCustomVariable.builder()
        .setNumber(config.getVariableType())
        .setType(config.getDataType())
        .setName(config.getReportName())
        .build();
  }

  /**
   * Adapter pattern to convert {@link SheetCustomVariable} to {@link
   * UserDefinedVariableConfiguration} by remapping fields.
   *
   * @param sheetCustomVariable custom user defined variable in Floody
   * @return the UserDefinedVariableConfiguration representation of SheetCustomVariable
   */
  public static UserDefinedVariableConfiguration transform(
      SheetCustomVariable sheetCustomVariable) {
    return new UserDefinedVariableConfiguration()
        .setDataType(sheetCustomVariable.getType())
        .setVariableType(sheetCustomVariable.getNumber())
        .setReportName(sheetCustomVariable.getName());
  }
}

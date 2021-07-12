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

import static org.apache.commons.lang3.StringUtils.isBlank;

/** The tag format of generated Floodlight - HTML or XHTML. */
public enum TagFormat {
  HTML,
  XHTML;

  /** Returns a TagFormat by matching value or Default Value if null. */
  public static TagFormat valueOfOrDefault(String val) {
    if (isBlank(val)) {
      return HTML;
    }

    return valueOf(val);
  }
}

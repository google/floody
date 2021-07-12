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

/**
 * DCM Floodlight cache busting methods as described in <a
 * href="https://developers.google.com/doubleclick-advertisers/v3.1/floodlightActivities#resource">floodlightActivity
 * Resource</a>.
 */
public enum CacheBustingType {
  ACTIVE_SERVER_PAGE,
  COLD_FUSION,
  JAVASCRIPT,
  JSP,
  NONE,
  PHP;

  /** Returns a CacheBusting by matching value or Default Value if null. */
  public static CacheBustingType valueOfOrDefault(String obj) {
    if (isBlank(obj)) {
      return NONE;
    }

    return valueOf(obj);
  }
}

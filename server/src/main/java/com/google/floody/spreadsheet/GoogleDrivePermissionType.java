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

package com.google.floody.spreadsheet;

/**
 * Enumeration to define the Drive Permission type as User or Group the {@code toString} method is
 * overridden to the same values as expected by the Drive API.
 */
public enum GoogleDrivePermissionType {
  GROUP {
    @Override
    public String getDrivePermissionTypeValue() {
      return "group";
    }
  },
  USER {
    @Override
    public String getDrivePermissionTypeValue() {
      return "user";
    }
  };

  /** Returns the permission type value to be used in making API calls to Google Drive */
  public abstract String getDrivePermissionTypeValue();
}

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

import java.io.IOException;

/** Encapsulates issues/errors in sharing files using Drive API. */
public class SharingException extends IOException {

  public SharingException() {}

  public SharingException(String message) {
    super(message);
  }

  public SharingException(String var1, Throwable var2) {
    super(var1, var2);
  }

  public SharingException(Throwable var1) {
    super(var1);
  }
}

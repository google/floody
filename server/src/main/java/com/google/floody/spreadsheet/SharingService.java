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

/** Contract to define methods for using File Sharing using Drive services. */
public interface SharingService {

  /**
   * Add the given Groups as editors to the specified file.
   *
   * @param fileId the file Identifier of the intended file
   * @param groups the list of groups to grant editor permissions, throws {@link
   *     IllegalArgumentException} when the empty or null
   * @throws IOException when there is an error in setting permissions
   */
  void addGroupsToFileAsEditors(String fileId, Iterable<String> groups) throws IOException;

  /**
   * Add the given users as editors to the specified file.
   *
   * @param fileId the file Identifier of the intended file
   * @param users the users to grant editor permissions, throws {@link IllegalArgumentException}
   *     when the empty or null
   * @throws IOException when there is an error in setting permissions
   */
  void addUsersToFileAsEditors(String fileId, Iterable<String> users) throws IOException;
}

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

/**
 * @fileoverview External declarations for using Drive API v2 library.
 * @see https://developers.google`.com/drive/v2/reference/
 *
 * These are not guaranteed to be up to date if that API changes.
 *
 * @externs
 * @suppress {strictMissingProperties} Namespaces declared without const
 */

/** @const */
gapi.client.drive = {};

/**
 * The metadata for the user's applications.
 */
gapi.client.drive.apps = {};

/**
 * Lists the applications and associated app metadata for the user.
 */
gapi.client.drive.apps.list = function (opt_params) {
};

/**
 * An item with user information and settings.
 */
gapi.client.drive.about = {};

/**
 * Gets the information about the current user along with Drive API settings.
 */
gapi.client.drive.about.get = function (opt_params) {
};

/**
 * Representation of a change to a file.
 */
gapi.client.drive.changes = {};

/**
 * Lists the changes for a user.
 */
gapi.client.drive.changes.list = function (opt_params) {
};

/**
 * The metadata for a file.
 */
gapi.client.drive.files = {};

/**
 * Get's the metadata for a particular file.
 */
gapi.client.drive.files.get = function (params) {
};

/**
 * Insert a new Drive item.
 */
gapi.client.drive.files.insert = function (opt_params) {
};

/**
 * Updates file metadata and/or content of an existing Drive item.
 */
gapi.client.drive.files.update = function (params) {
};

/**
 * Creates a copy of the specified Drive item.
 */
gapi.client.drive.files.copy = function (params) {
};

/**
 * Lists the user's files
 */
gapi.client.drive.files.list = function (opt_params) {
};

/**
 * Moves a file to the trash.
 */
gapi.client.drive.files.trash = function (opt_params) {
};

/**
 * Namespace for permission related operations.
 */
gapi.client.drive.permissions = {};

/**
 * Lists permissions of the specified Drive item.
 */
gapi.client.drive.permissions.list = function (params) {
};

/**
 * Namespace for revision related operations.
 */
gapi.client.drive.revisions = {};

/**
 * Lists revisions of the specified Drive item.
 */
gapi.client.drive.revisions.list = function (params) {
};

/**
 * Updates a revision of the specified Drive item revision.
 */
gapi.client.drive.revisions.update = function (params) {
};

/**
 * Namespace for parent related operations. Used for fetching parent metadata
 * and adding/removing parents.
 */
gapi.client.drive.parents = {};

/**
 * Adds a parent to a Drive item.
 */
gapi.client.drive.parents.insert = function (params) {
};
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
 * @fileoverview Provide gapi.auth2 public api.
 *
 * @externs
 */

gapi.auth2 = {};

/**
 * Requests authorization for the specified scopes.
 * @param {!gapi.auth2.AuthorizeConfig} options Authorize options.
 * @param {!function(!gapi.auth2.AuthorizeResponse)} callback Callback called
 *     when authorize flow finished.
 */
gapi.auth2.authorize = function (options, callback) {
};

/**
 * Initializes the base auth object. Fields will default to the values specified
 * by the meta tags.
 * @param {gapi.auth2.ClientConfig=} opt_options Initialization options.
 * @return {!gapi.auth2.GoogleAuth}
 */
gapi.auth2.init = function (opt_options) {
};

/**
 * Enables Idp-iframe debug logs (js console).
 * @param {boolean=} opt_value
 */
gapi.auth2.enableDebugLogs = function (opt_value) {
};

/**
 * @return {gapi.auth2.GoogleAuth}
 */
gapi.auth2.getAuthInstance = function () {
};

/**
 * Basic profile of a particular user.
 * @param {Object} token id_token from idp iframe 'tokenReady' event.
 * @constructor
 */
gapi.auth2.BasicProfile = function (token) {
};

/**
 * Getter for obfuscated gaia id.
 * @return {string} Obfuscated gaia id.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getId = function () {
};

/**
 * Getter for name.
 * @return {string} User name.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getName = function () {
};

/**
 * Getter for givenName.
 * @return {string} User's given name.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getGivenName = function () {
};

/**
 * Getter for familyName.
 * @return {string} User's family name.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getFamilyName = function () {
};

/**
 * Getter for imageUrl.
 * @return {string} Url for profile picture.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getImageUrl = function () {
};

/**
 * Getter for email.
 * @return {string} Email address.
 * @public
 */
gapi.auth2.BasicProfile.prototype.getEmail = function () {
};

/**
 * The root interface for all auth-related functionality.
 * @param {gapi.auth2.ClientConfig} options Initialization options.
 * @constructor
 */
gapi.auth2.GoogleAuth = function (options) {
};

/**
 * Attaches click handler to element.
 * @param {!(Element|string)} container
 * @param {(gapi.auth2.SigninOptions|gapi.auth2.SigninOptionsBuilder|Function)=}
 *     opt_options Signin options.
 * @param {?Function=} opt_resolveCallback Function accepting auth result
 *     executed on successful auth resolution through click on the targeted
 *     container.
 * @param {?Function=} opt_rejectCallback Function accepting auth result
 *     executed on failed auth resolution through click on the targeted
 *     container.
 * @return {undefined}
 * @export
 */
gapi.auth2.GoogleAuth.prototype.attachClickHandler = function (container,
    opt_options, opt_resolveCallback, opt_rejectCallback) {
};

/**
 * The “currentUser” account. We're initializing it with GoogleUser
 * instance which has all properties equal null. Notice that this instance
 * will never be replaced but will be updated instead (GoogleUser.update).
 * @public @const {!gapi.auth2.LiveValue<!gapi.auth2.GoogleUser>}
 */
gapi.auth2.GoogleAuth.prototype.currentUser;

/**
 * Disconnects user by revoking token.
 * @return {!IThenable<undefined>}
 */
gapi.auth2.GoogleAuth.prototype.disconnect = function () {
};

/**
 * Requests code for offline access.
 * 3P can pass redirect_uri as uri or postmessage.
 * If redirect_uri != postmessage:
 * Otherwise code will be returned by Promise.
 *
 * @param {gapi.auth2.OfflineAccessOptions=} opt_options
 * @return {!IThenable<gapi.auth2.CodeResponse>}
 */
gapi.auth2.GoogleAuth.prototype.grantOfflineAccess = function (opt_options) {
};

/**
 * isSignedIn is per all sessions. signedIn = true only if currentUser
 * contains valid token. Note that before idp-iframe sends first
 * tokenReady/noSessionBound event, we are assuming that user is
 * not signed in.
 *  * @public @const {!gapi.auth2.LiveValue<boolean>}
 */
gapi.auth2.GoogleAuth.prototype.isSignedIn;

/**
 * Sign in a new user.
 * popup!
 * @param {?Object=}
 *     opt_options Signin options.
 * @return {!IThenable.<!gapi.auth2.GoogleUser>}
 */
gapi.auth2.GoogleAuth.prototype.signIn = function (opt_options) {
};

/**
 * Signs out all accounts.
 * @return {!IThenable}
 */
gapi.auth2.GoogleAuth.prototype.signOut = function () {
};

/**
 * Resolves with googleAuth instance when googleAuth is fully initialized.
 * It means idp-iframe is initialized and googleAuth received and processed
 * immediate_mode = true auth results.
 * @param {(function(!gapi.auth2.GoogleAuth):R)=} opt_onFulfilled
 * @param {Function=} opt_onRejected
 * @param {Object=} opt_context
 * @return {!IThenable<R|undefined>}
 * @template R
 */
gapi.auth2.GoogleAuth.prototype.then = function (opt_onFulfilled,
    opt_onRejected,
    opt_context) {
};

/**
 * Gets scopes provided through gapi.auth2.init.
 * @return {string} scopes
 */
gapi.auth2.GoogleAuth.prototype.getInitialScopes = function () {
};

/**
 * Object representing a user, and the primary interface for clients
 * to do actions with or on behalf of the user.
 * @constructor
 * @param {?gapi.auth2.AuthResponse} authResponse
 */
gapi.auth2.GoogleUser = function (authResponse) {
};

/**
 * Requests that the user grant the specified scopes in addition to any already
 * granted scopes.
 * Opens popup.
 * @param {!(gapi.auth2.SigninOptions|gapi.auth2.SigninOptionsBuilder)} options
 * @return {!IThenable<gapi.auth2.GoogleUser>}
 */
gapi.auth2.GoogleUser.prototype.grant = function (options) {
};

/**
 * Gets id - currently, it's obfuscatedGaiaId.
 * @return {?string}
 */
gapi.auth2.GoogleUser.prototype.getId = function () {
};

/**
 * @return {boolean}  Whether a valid authResponse is available.
 */
gapi.auth2.GoogleUser.prototype.isSignedIn = function () {
};

/**
 * Returns authResponse. This function is an equivalent to
 * gapi.auth.getToken() from old implementation.
 * @param {boolean=} opt_includeAccessToken Whether to include accessToken.
 * @return {?gapi.auth2.AuthResponse}
 */
gapi.auth2.GoogleUser.prototype.getAuthResponse =
    function (opt_includeAccessToken) {
    };

/**
 * Getter for getting basic profile of this user.
 * @return {gapi.auth2.BasicProfile}
 * @public
 */
gapi.auth2.GoogleUser.prototype.getBasicProfile = function () {
};

/**
 * Returns list of granted scopes.
 * @return {string}
 */
gapi.auth2.GoogleUser.prototype.getGrantedScopes = function () {
};

/**
 * Gets hosted_domain. Hosted domain is not null only with dasher accounts.
 * @return {?string}
 */
gapi.auth2.GoogleUser.prototype.getHostedDomain = function () {
};

/**
 * Requests that the user grant the specified scopes in addition to any already
 * granted scopes.
 * Opens popup.
 * @param {gapi.auth2.OfflineAccessOptions=} opt_options Options for offline
 *     access.
 * @return {!IThenable<gapi.auth2.CodeResponse>} Promise containing code and
 *     scope for offline access.
 */
gapi.auth2.GoogleUser.prototype.grantOfflineAccess = function (opt_options) {
};

/**
 * Checks whether the user has granted all of the scopes listed.
 * @param {string} scopes Space delimited list of scopes.
 * @return {boolean} Whether all of the scopes are granted.
 */
gapi.auth2.GoogleUser.prototype.hasGrantedScopes = function (scopes) {
};

/**
 * Forces a refresh of the access token, and then returns a Promise for the
 * new AuthResponse.
 * @return {!IThenable<!gapi.auth2.AuthResponse>}
 */
gapi.auth2.GoogleUser.prototype.reloadAuthResponse = function () {
};

/**
 * A wrapper around a variable that provides an interface to observe changes in
 * that variable.
 * @constructor
 * @template TYPE
 * @param {TYPE=} opt_value
 */
gapi.auth2.LiveValue = function (opt_value) {
};

/**
 * Notifies a handler whenever the value changes.
 * @param {function(TYPE)} handler
 * @return {!gapi.auth2.LiveValue.Listener.<TYPE>}
 */
gapi.auth2.LiveValue.prototype.listen = function (handler) {
};

/**
 * Returns value stored by this instance of LiveValue
 * @return {TYPE} value
 * @export
 */
gapi.auth2.LiveValue.prototype.get = function () {
};

/**
 * Creates an options builder for Signin.
 * @param {?Object=} opt_obj
 *     Optional prebuild options or another builder.
 * @constructor
 */
gapi.auth2.SigninOptionsBuilder = function (opt_obj) {
};

/**
 * Gets the apppackagename param.
 * @return {!string}
 */
gapi.auth2.SigninOptionsBuilder.prototype.getAppPackageName = function () {
};

/**
 * Sets the apppackagename param.
 * @param {string} apppackagename
 * @return {!gapi.auth2.SigninOptionsBuilder} The current SigninOptions.
 */
gapi.auth2.SigninOptionsBuilder.prototype.setAppPackageName =
    function (apppackagename) {
    };

/**
 * Gets the scope param.
 * @return {!string} Scope string.
 */
gapi.auth2.SigninOptionsBuilder.prototype.getScope = function () {
};

/**
 * Sets the scope param of SigninOptionsBuilder.
 * @param {string} scopeString Space-separated list of scopes.
 * @return {!gapi.auth2.SigninOptionsBuilder} The current SigninOptions.
 */
gapi.auth2.SigninOptionsBuilder.prototype.setScope = function (scopeString) {
};

/**
 * Gets the prompt param.
 * @return {!string}
 */
gapi.auth2.SigninOptionsBuilder.prototype.getPrompt = function () {
};

/**
 * Sets the prompt param.
 * @param {string} prompt
 * @return {!gapi.auth2.SigninOptionsBuilder} The current SigninOptions.
 */
gapi.auth2.SigninOptionsBuilder.prototype.setPrompt =
    function (prompt) {
    };

/**
 * @return {!gapi.auth2.SigninOptions} The constructed options.
 */
gapi.auth2.SigninOptionsBuilder.prototype.get = function () {
};

//******************************************************************************
// Implied exports (e.g, types used in signatures of exported methods)
//******************************************************************************

/**
 * An object representing a listener which can request to stop listening.
 * @constructor
 * @template TYPE
 * @param {function(TYPE)} handler
 */
gapi.auth2.Listener = function (handler) {
};

/**
 * A LiveValue-specific Listener.
 * @constructor
 * @template TYPE
 * @extends {gapi.auth2.Listener.<TYPE>}
 * @param {!gapi.auth2.LiveValue.<TYPE>} liveValue
 * @param {function(TYPE)} handler
 */
gapi.auth2.LiveValue.Listener = function (liveValue, handler) {
};

//******************************************************************************
// Interfaces
//******************************************************************************

//******************************************************************************
// Typedefs
//******************************************************************************

/** @record */
gapi.auth2.ClientConfig = function () {
};

/** @record */
gapi.auth2.AuthorizeConfig = function () {
};

/** @record */
gapi.auth2.AuthResponse = function () {
};

/** @record */
gapi.auth2.AuthorizeResponse = function () {
};

/** @record */
gapi.auth2.CodeResponse = function () {
};

/** @record */
gapi.auth2.ParsedCookiePolicy = function () {
};

/** @record */
gapi.auth2.SigninOptions = function () {
};

/** @record */
gapi.auth2.OfflineAccessOptions = function () {
};

//******************************************************************************
// Enums
//******************************************************************************
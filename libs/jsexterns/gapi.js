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
 * @fileoverview External declarations for using the Apiary js client library.
 * @see http://code.google.com/p/google-api-javascript-client/
 *
 * @author jacobly@google.com (Jacob Lee)
 * @externs
 */

/**
 * The main namespace.
 * @suppress {const|duplicate}
 */
var gapi;

/** @const {!Object}*/
gapi.client = {};

/**
 * @enum {string}
 */
gapi.client.AuthType = {};

/**
 * Use OAuth2 if possible, and fallback to 1p auth if globally configured.
 * @type {string}
 */
gapi.client.AuthType.AUTO;

/**
 * No auth.
 * @type {string}
 */
gapi.client.AuthType.NONE;

/**
 * Use OAuth2 if token is available, no auth otherwise.
 * @type {string}
 */
gapi.client.AuthType.OAUTH2;

/**
 * Use 1p auth.
 * @type {string}
 */
gapi.client.AuthType.FIRST_PARTY;

/**
 * Type encapsulating client init config.
 * apiKey: the api-key generated from Google API console.
 * discoveryDocs: an array of discovery URLs or documents.
 * clientId: the OAuth client id used to authenticate user.
 * scope: the OAuth scope string. It must be a space-separated list of
 *        individual OAuth scopes. If you have an array of scopes, you can use
 *        `.join(' ')` to generate a single string.
 * fetchBasicProfile: whether to fetch basic user profile by adding scopes
 *                    "profile", "email", and "openid". Default is true.
 * Keys are strings to allow use of this struct from separate compilation units.
 * @typedef {{
 *   'apiKey': (?string|undefined),
 *   'discoveryDocs': (?Array<string|!Object>|undefined),
 *   'clientId': (?string|undefined),
 *   'scope': (?string|undefined),
 *   'fetchBasicProfile': (boolean|undefined)
 * }}
 */
gapi.client.InitConfig;

/**
 * Initializes client with api-key, discovery, and OAuth client id and scope.
 * If OAuth client id and scope are provided, this function will load the
 * auth2 module and call gapi.auth2.init to initialize OAuth.
 * @param {!gapi.client.InitConfig} config A config object for initialization.
 * @return {!IThenable<undefined>}
 */
gapi.client.init = function (config) {
};

/**
 * Loads an API by fetching its Discovery doc and registering all methods
 * described by the doc.
 * @param {string|!Object} nameOrDoc The name of the API to load, a discovery
 *     URL to load, or a discovery document.
 * @param {?string=} opt_version The optional version of the API to load.
 *     The default is "v1".
 * @param {?Function=} opt_callback An optional callback method to
 *     call when the API is loaded and its methods are registered.
 * @param {?(Object<string, string>|string)=} opt_params Optional parameters
 *     to customize loading, or an API server from which to load the document.
 */
gapi.client.load = function (
    nameOrDoc, opt_version, opt_callback, opt_params) {
};

/**
 * Creates a new batch object for batching RPC requests.
 * @return {!gapi.client.Batch|!gapi.client.RpcBatch} The batch object.
 */
gapi.client.newBatch = function () {
};

/**
 * Creates a new batch object for batching HTTP requests.
 * @return {!gapi.client.Batch} The batch object.
 * @deprecated This is deprecated. Switch to {@link gapi.client.newBatch}.
 */
gapi.client.newHttpBatch = function () {
};

/**
 * Creates a new batch object for batching RPC requests.
 * @return {!gapi.client.RpcBatch} The batch object.
 * @deprecated This is deprecated and uses JSON-RPC. Switch to
 *     {@link gapi.client.newBatch}.
 */
gapi.client.newRpcBatch = function () {
};

/**
 * Registers the given method under the gapi.client namespace. For example
 * "buzz.people.get" is exported as "gapi.client.buzz.people.get". When
 * called, the registered method returns a gapi.client.RpcRequest object. The
 * RPC can then be executed by calling
 * gapi.client.RpcRequest.execute(callback).
 * @param {string} method The method to register.
 * @param {({
 *           root: (string|undefined),
 *           apiVersion: (string|undefined)
 *        }|{
 *           servicePath: string,
 *           restPath: string,
 *           httpMethod: string,
 *           parameters: (Object|undefined),
 *           parameterName: (string|undefined),
 *           supportsSubscription: (boolean|undefined),
 *           root: (string|undefined)
 *        })=} opt_params For an old-style RPC method, the parameter is optional
 *     and has the properties listed in the first record type: 'root' is the API
 *     server to which to send method calls, and 'apiVersion' is the version of
 *     the API for this method. For a new-style REST method, the parameter is
 *     required and has the properties listed in the second record type.
 * @deprecated This is deprecated and uses JSON-RPC. Switch to
 *     {@link gapi.client.init} or {@link gapi.client.request}.
 */
gapi.client.register = function (method, opt_params) {
};

/**
 * Creates a request.
 * @param {string|gapi.client.Request.RequestParams} pathOrArgs The path to
 *     make the request, or a key-value pair object encapsulating the arguments
 *     for this method. See the definition of
 *     gapi.client.Request.RequestParams for more details.
 * @return {!gapi.client.Request|null} The Request object which can be added
 *     to a batch request or executed by calling execute(callback), or null
 *     if the 'callback' field was present in pathOrArgs.
 */
gapi.client.request = function (pathOrArgs) {
};

/**
 * Creates a generic deprecated RPC request for the given method, version, and
 * params.
 * @param {string} method The RPC method to execute.
 * @param {string} version Optional version of the API to execute. Defaults to
 *     "v1".
 * @param {Object=} opt_rpcParams Optional key-value pairing of parameters for
 *     this RPC.
 * @return {!gapi.client.RpcRequest} The RPC request object representing this
 *     RPC.
 * @deprecated This is deprecated and uses JSON-RPC. Switch to
 *     {@link gapi.client.init} or {@link gapi.client.request}.
 */
gapi.client.rpcRequest = function (method, version, opt_rpcParams) {
};

/**
 * Stores API developer key to be sent with subsequent API call HTTP requests.
 * @param {undefined|?string=} opt_apiKey The developer key identifying this
 *     application to the developer console. Sets the 'key' URL
 *     parameter. Omitting the parameter or passing undefined removes any
 *     previously-saved API key.
 */
gapi.client.setApiKey = function (opt_apiKey) {
};

/**
 * Sets versions for APIs. Overwrites individual values, but not the full map.
 * @param {undefined|?Object<string, string>=} opt_versions A mapping of service
 *     name or method to version, for example {'plus.people.search': 'v1',
 *     'oauth2': 'v3' }. Omitting the parameter or passing undefined removes any
 *     previously-saved API versions.
 * @deprecated This is deprecated and uses JSON-RPC. Switch to
 *     {@link gapi.client.init} or {@link gapi.client.request}.
 */
gapi.client.setApiVersions = function (opt_versions) {
};

/**
 * Retrieves an access token that was previously stored by gapi.auth, gapi.auth2
 * or manually by calling setToken. It uses the key given or the default key.
 * @param {string=} opt_key Optional Key of the token retrieved.
 * @return {?Object} Access token.
 */
gapi.client.getToken = function (opt_key) {
};

/**
 * Stores an access token using the key given or the default key. If null is
 * passed as a token, it will remove the token previously stored at the given
 * key.
 * @param {?Object} token The access token to
 *     save.
 * @param {string=} opt_key Optional Key of the token retrieved.
 */
gapi.client.setToken = function (token, opt_key) {
};

/**
 * Type encapsulating an HTTP response.
 * @typedef {{
 *   result: *,
 *   body: string,
 *   headers: (Object<string, string>),
 *   status: (?number),
 *   statusText: (?string)
 * }}
 */
gapi.client.Response;

/**
 * @typedef {function(*,string)|function(*)}
 */
gapi.client.Callback;

/**
 * @see gapi.client.request()
 * @implements {IThenable<gapi.client.Response>}
 * @unrestricted
 */
gapi.client.Request = class {
  /**
   * @param {string|gapi.client.Request.RequestParams} pathOrArgs The
   *     path to make the request, or a key-value pair object
   *     encapsulating the arguments for this method.
   */
  constructor(pathOrArgs) {
  }

  /**
   * Executes the request and returns a promise.
   * @override
   */
  then(opt_onFulfilled, opt_onRejected, opt_context) {
  }

  /**
   * Executes the request and returns a promise.
   * @return {!IThenable<gapi.client.Response>}
   */
  getPromise() {
  }

  /**
   * Executes the request encapsulated by this object and applies the given
   * callback with the response.
   * @param {!gapi.client.Callback} callback The callback to execute
   *     when the request returns.
   */
  execute(callback) {
  }
};

/**
 * Type encapsulating a request's params. The path key is required, the rest are
 * optional.The values are described in detail below.
 * <ul>
 *   <li>path: The URL to handle the request.</li>
 *   <li>method: The HTTP request method to use. Default is 'GET'.</li>
 *   <li>params: URL params in key-value pair form.</li>
 *   <li>headers: Additional HTTP request headers.</li>
 *   <li>body: The HTTP request body (applies to PUT or POST).</li>
 *   <li>root: The API server to which to send the request.</li>
 *   <li>
 *     callback: If supplied, the request is executed immediately and no
 *     gapi.client.Request object is returned.
 *   </li>
 *   <li>responseType: XMLHttpRequest responsetype.</li>
 *   <li>authType: Authorization mode for the request.</li>
 * </ul>
 * @typedef {{
 *   path: string,
 *   method: (?string|undefined),
 *   params: (Object<string, *>|undefined),
 *   headers: (Object<string, string>|undefined),
 *   body: (?string|!Object|undefined),
 *   root: (string|undefined),
 *   callback: (Function|undefined),
 *   responseType: (?string|undefined),
 *   authType: (gapi.client.AuthType|undefined)
 * }}
 */
gapi.client.Request.RequestParams;

/**
 * @constructor
 * @extends {gapi.client.Request}
 * @deprecated Use Request.
 */
gapi.client.HttpRequest = gapi.client.Request;

/**
 * @typedef {gapi.client.Request.RequestParams}
 * @deprecated Use Request.RequestParams.
 */
gapi.client.HttpRequest.RequestParams;

/**
 * @deprecated Use gapi.client.Request.
 * @unrestricted
 */
gapi.client.RpcRequest = class {
  /**
   * @param {{method: string,
   *          rpcParams: Object,
   *          transport: ?{name: string, root: ?string},
   *          root: (string|undefined),
   *          apiVersion: (string|undefined)
   *        }} rpcArgs
   *     An object containing the params for this request. 'method' is
   *     the fluent- style method to execute, 'rpcParams' is the params
   *     to execute with the method, 'transport' is an optional object
   *     with two properties: 'name' is a unique name for this
   *     transport, 'root' is an optional API server to send the
   *     request to, and 'apiVersion' is the optional version for this request's
   *     service.
   */
  constructor(rpcArgs) {
  }

  /**
   * Executes the request encapsulated by this object and applies the given
   * callback with the response.
   * @param {!function(Object)} callback The callback to execute when the
   *     request returns.
   * @deprecated Use gapi.client.Request.prototype.execute().
   */
  execute(callback) {
  }
};

/**
 * HTTP batch constructor
 * @implements {IThenable<Object<string, gapi.client.Response>>}
 * @const
 * @unrestricted
 */
gapi.client.Batch = class {
  constructor() {
  }

  /**
   * Executes the request and returns a promise.
   * @override
   */
  then(opt_onFulfilled, opt_onRejected, opt_context) {
  }

  /**
   * Adds a request to the batch. ID and callback are both optional. If an ID is
   * not provided one is assigned automatically.
   * @param {gapi.client.Request} request The HTTP request to add to the
   *     batch.
   * @param {{
   *          id: (string|undefined),
   *          callback: (function()|undefined)
   *        }=} opt_args
   *     Optional extra parameters to add a request to the batch.
   *     'id' is an optional ID to identify the entry in the batch, and
   *     'callback' is an optional callback to execute with the response.
   *     The callback function takes two parameters, the batch ID and the
   *     individual response.
   * @return {string} ID of the newly added entry.
   */
  add(request, opt_args) {
  }

  /**
   * Executes the batched request. Individual callbacks for each batch entry are
   * executed with the individual response. The callback provided to this
   * function executes with the entire batched response.
   * @param {gapi.client.Callback=} opt_callback The optional callback for the
   *     batched request.
   */
  execute(opt_callback) {
  }
};

/**
 * @deprecated Use Batch.
 * @unrestricted
 */
gapi.client.HttpBatch = class {
  constructor() {
  }
};

/**
 * RPC batch constructor
 * @deprecated Use Batch.
 * @unrestricted
 */
gapi.client.RpcBatch = class {
  constructor() {
  }

  /**
   * Adds a request to the batch. ID and callback are both optional. If an ID is
   * not provided one is assigned automatically.
   * @param {gapi.client.RpcRequest} request The RPC request to add to the
   *     batch.
   * @param {{
   *          id: (string|undefined),
   *          callback: (function()|undefined)
   *        }=} opt_args
   *     Optional extra parameters to add a request to the batch.
   *     'id' is an optional ID to identify the entry in the batch, and
   *     'callback' is an optional callback to execute with the response.
   *     The callback function takes two parameters, the batch ID and the
   *     individual response.
   * @return {string} ID of the newly added entry.
   * @deprecated Use Batch.prototype.add().
   */
  add(request, opt_args) {
  }

  /**
   * Executes the batched request. Individual callbacks for each batch entry are
   * executed with the individual response. The callback provided to this
   * function executes with the entire batched response.
   * @param {function(Object)=} opt_callback The optional callback for the
   *     batched request.
   * @deprecated Use Batch.prototype.execute().
   */
  execute(opt_callback) {
  }
};

/** @const */
gapi.auth = {};

/**
 * The OAuth 2.0 token object represents the OAuth 2.0 token and any associated
 * data.
 * @see https://developers.google.com/api-client-library/javascript/reference/referencedocs#OAuth20TokenObject
 * @typedef {{
 *   access_token: (string|undefined),
 *   error: (string|undefined),
 *   expires_in: (string|undefined),
 *   scope: (string|Array<string>|undefined)
 * }}
 */
gapi.auth.OAuth20TokenObject;

/**
 * Initiates a login request by opening a popup window with the
 * authorization or by opening an iframe if immediate mode was
 * requested.
 * @see https://developers.google.com/api-client-library/javascript/reference/referencedocs#gapiauthauthorize
 * @param {Object=} opt_params The request parameters.
 * @param {!function(!gapi.auth.OAuth20TokenObject)=} opt_callback An optional
 *     callback to call once auth is completed.
 */
gapi.auth.authorize = function (opt_params, opt_callback) {
};

/**
 * Checks if the current session is still valid for the given session state and
 * client ID.
 * @param {{
 *   session_state: ?string,
 *   client_id: ?(string|undefined)
 * }} params An object containing the params necessary to check the session
 *     state.
 *     <ul>
 *       <li>session_state: The current session state. If null, this method
 *       returns true if there is no signed-in Google session, false otherwise.
 *       </li>
 *       <li>client_id: The client ID for the application. This value is
 *       optional and not provided for first party sessions.</li>
 *     </ul>
 * @param {!function(boolean)} callback The callback to execute when the
 *     check is complete. The callback takes a boolean param indicating
 *     the success of the check.
 */
gapi.auth.checkSessionState = function (params, callback) {
};

/**
 * Sets up the relay iframe.  This iframe is on the same domain as the callback,
 * so the callback can talk to it directly.  This frame then RPCs to this page
 * with the oauth callback URL.
 * @param {function()} relayReadyContinuation what to do when the
 *     relay frame is ready.
 */
gapi.auth.init = function (relayReadyContinuation) {
};

/**
 * Retrieves the token from the cookie.
 * @param {string=} opt_key The key to load.
 * @param {boolean=} opt_returnErrors If true, error tokens will be returned.
 * @return {?gapi.auth.OAuth20TokenObject} Returns the token matching the key
 *     (if a key is provided), or the first token (if no key is provided), or
 *     null (if the token is not found).
 */
gapi.auth.getToken = function (opt_key, opt_returnErrors) {
};

/**
 * Sets the token to the given value.
 * @param {Object|string} tokenOrKey if a string, then token key.
 *     Otherwise, the token itself, in which case the key is defaulted to
 *     'token'.
 * @param {Object=} opt_token The token.
 * @param {boolean=} opt_write Whether to write the token to the cookie.
 *     Defaults to true when the token is false-y and false otherwise.
 */
gapi.auth.setToken = function (tokenOrKey, opt_token, opt_write) {
};

/**
 * Retrieves OAuth 2 token VersionInfo for given session index using 1st party
 * authentication. The VersionInfo (when present) is necessary to ensure that
 * the auth state verification is consistent after the Gaia tokens are migrated
 * to Kansas. See go/lsolh for details.
 *
 * @param {function(?string)} callback the callback to invoke with the
 *     result; the parameter, if present, will be a Kansas VersionInfo for
 *     the most recent authorization event for this user in this browser
 *     session.
 * @param {string} sessionIndex the index to the user's active Gaia session.
 */
gapi.auth.getVersionInfo = function (callback, sessionIndex) {
};

/**
 * If the user is logged in, this will return a value for
 * the Authorization header used in First-Party authentication.
 * @param {Array.<{key:string,value:string}>=} opt_userIdentifiers
 * @return {?string} the computed header value or null.
 */
gapi.auth.getAuthHeaderValueForFirstParty = function (opt_userIdentifiers) {
};
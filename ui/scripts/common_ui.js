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

goog.module('common.logic');

const safe = goog.require('goog.dom.safe');
const soy = goog.require('goog.soy');
const templates = goog.require('floodyUi.floody');

/** Floody Configuration */
const FLOODY_API_ENDPOINT = '';

/**
 * Required OAuth Scopes for Floody backend.
 *
 * @type {!Array<string>}
 */
const OAUTH_SCOPES = [
  'email',
  'profile',
  'https://www.googleapis.com/auth/dfatrafficking',
  'https://www.googleapis.com/auth/tagmanager.edit.containers',
];

const Routes = {
  LOADING: 'loading',
  HOMEPAGE: 'homepage',
  FILESELECT: 'fileSelect',
  CREATENEW: 'createNew',
};

/** The global store. Keeps track of data changes. */
class StoreType {
  constructor() {
    this.clientId = '';
    this.route = Routes.LOADING;
    this.isSignedIn = false;
    this.email = '';
    this.profilePicture = '';
    this.profiles = null;
    this.selectedProfile = '';
    this.accounts = null;
    this.selectedAccount = '';
    this.floodlightConfigs = null;
    this.selectedFloodlightConfig = '';
    this.recentFiles = [];
    this.errorMessage = '';
    this.validProfiles = null;
    this.spreadsheetTitle = null;
    this.selectedProfileId = '';
    this.spreadsheetTitle = '';
    this.userAuthStatus = '';
    this.userAuthHelpMessage = '';
  }

  /**
   * Validation check to make sure the stored values for all necessary fields
   * are correctly assigned.
   * @return {boolean}: Whether the contents are valid or not.
   */
  storeContentsAreValid() {
    // TODO: Validate that the values are type safe. Low priority.
    const isInvalid =
        this.selectedProfile === '' ||
        this.selectedAccount === '' ||
        this.selectedFloodlightConfig === '';

    return !isInvalid;
  }

  /**
   * Enables setting the next page for the simple page router
   * @param {string} route: the path of the next page.
   */
  setRoute(route) {
    this.route = route;
    console.log(`Routing to ${route}`);
    throwEvent('route-updated');
  }
}

/**
 * Common Store which provides local storage implementation.
 *
 * @type {!StoreType}
 */
const STORE = new StoreType();

/**
 * Throws generic events
 *
 * @param {string} name: the name of the error to throw as event.
 */
function throwEvent(name) {
  window.dispatchEvent(new Event(name));
}

/** Checks the protocol and upgrades to HTTPS if not secure. */
function checkAndUpgradeSsl() {
  if (window.location.protocol !== 'https:') {
    window.location.protocol = 'https:';
  }
}
/**
 * Formats a timestamp into relative strings (e.g. "today", "22 days ago", etc.)
 *
 * @param {string} datetimeString: The datetime in a string format
 * @return {string} A relative time string.
 */
function daysDifferenceFromToday(datetimeString) {
  const rtf = new Intl.RelativeTimeFormat('en', {
    localeMatcher: 'best fit',
    numeric: 'auto',
    style: 'short',
  });
  const today = new Date().getTime();
  const datetime = new Date(Date.parse(datetimeString)).getTime();
  const timeDeltaDays = (datetime - today) / (24 * 60 * 60 * 1000);
  return rtf.format(Math.round(timeDeltaDays), 'days');
}

/** Handles client loading when gAPI has loaded. */
function handleClientLoad() {
  gapi.load('client:auth2', retrieveClientId);
}

function retrieveClientId() {
  fetch(`${FLOODY_API_ENDPOINT}/admin/clientId`, {
    headers: {Accept: 'application/json'},
  })
  .then(response => response.json())
  .then(clientInformationJson => {
    STORE.clientId = clientInformationJson['clientId'].trim();
    initGapiClient();
  });
}

/** Initiates the gAPI client and captures sign-in status. */
function initGapiClient() {
  gapi.client
  .init({
    clientId: STORE.clientId,
    scope: OAUTH_SCOPES.join(' '),
  })
  .then(() => {
    gapi.auth2.getAuthInstance().isSignedIn.listen(updateSigninStatus);
    updateSigninStatus(gapi.auth2.getAuthInstance().isSignedIn.get());
  });
}

/**  Handles the user clicking on the "Sign In" button. */
function handleSignInClick() {
  gapi.auth2.getAuthInstance().signIn();
}

/**
 * Handles the loading of header bar.
 *
 * @param {!string} barType: states the type of the bar to be rendered, either manage or gtm.
 */
function loadHeaderBar(barType) {
  const headerDiv = document.getElementById('header');
  soy.renderElement(headerDiv, templates.headerBar, {
    isSignedIn: STORE.isSignedIn,
    profilePicture: STORE.profilePicture,
    barType: barType,
    selectedProfile: STORE.selectedProfileId,
    profiles: STORE.validProfiles,
    spreadsheetTitle: STORE.spreadsheetTitle,
    userAuthStatus: STORE.userAuthStatus,
    userAuthHelpMessage: STORE.userAuthHelpMessage,
  });
}

/**
 * Handles user sign-in.
 *
 * @param {?boolean} isSignedIn: Whether the user has been authenticated and is
 *  currently signed in.
 */
function updateSigninStatus(isSignedIn) {
  STORE.isSignedIn = isSignedIn;

  if (isSignedIn) {
    if (
        window.sessionStorage.getItem('prodCounsel') === undefined ||
        window.sessionStorage.getItem('prodCounsel') === null
    ) {
      throwEvent('display-product-counsel-message');
    }

    loadUserDetails();

    STORE.setRoute(Routes.FILESELECT);
    throwEvent('user-signed-in');
  } else {
    STORE.setRoute(Routes.HOMEPAGE);
  }
}

/** Loads user information. */
function loadUserDetails() {
  // TODO: create a single call to gapi
  STORE.email = getUser().getBasicProfile().getEmail();
  STORE.profilePicture = getUser().getBasicProfile().getImageUrl();
}

/** Handles the user clicking on the "Sign Out" button. */
function handleSignOutClick() {
  gapi.auth2.getAuthInstance().signOut();
  window.sessionStorage.removeItem('profileId');
  window.sessionStorage.removeItem('prodCounsel');
  safe.setLocationHref(window.location, window.location.origin);
}

/**
 * Shorthand function to retrieve user information from gAPI.
 * @return {!gapi.auth2.GoogleUser}: The GoogleUser object representing the currently
 * logged in user.
 */
function getUser() {
  return gapi.auth2.getAuthInstance().currentUser.get();
}

/**
 * Shorthand function to retrieve current OAuth token.
 * @return {string}: The oauth access token.
 */
function getOAuthToken() {
  return getUser().getAuthResponse()['access_token'];
}

/**
 * Retrieves the profile ID from session storage
 * @return {null|string} the profile ID
 */
function getProfileId() {
  return window.sessionStorage.getItem('profileId');
}

/**
 * Composes the config for Floody GET requests.
 * @return {!Object}: The Configuration for Floody API GET requests.
 */
function floodyGetConfig() {
  return {
    method: 'GET',
    mode: 'cors',
    cache: 'no-cache',
    credentials: 'same-origin',
    headers: {
      Authorization: `Bearer ${getOAuthToken()}`,
      profile: `${getProfileId()}`,
      Accept: 'application/json',
    },
  };
}

/**
 * Gets the most recent Floody files from the user's Google Drive.
 * @return {!Promise<!Array<{id: string, name: string, lastModified: string}>>}
 *     List of sheets accessible to the user.
 */
function getFloodyFiles() {
  const transformFloodySheetsToFloodyFiles = floodySheet => ({
    id: floodySheet.id,
    name: floodySheet.name,
    recency: daysDifferenceFromToday(floodySheet.lastModified),
  });

  return fetch('/user/recentSheets', floodyGetConfig())
  .then(response => response.json())
  .then(recentSheets => {
    if (!recentSheets['spreadsheets']) {
      return Promise.resolve([]);
    }

    return recentSheets['spreadsheets'].map(
        transformFloodySheetsToFloodyFiles
    );
  })
  .catch(error => renderError(error));
}

/**
 * Resets the Account properties in the STORE.
 */
function setDefaultAccountStoreProp() {
  STORE.selectedAccount = '';
  STORE.accounts = null;
}

/**
 * Resets the Floodlight properties in the STORE.
 */
function setDefaultFloodlightStoreProp() {
  STORE.selectedFloodlightConfig = '';
  STORE.floodlightConfigs = null;
}

/**
 * Sets the Parameters for the ActionBar in STORE.
 *
 * @param selectedProfileId
 * @param validProfiles
 * @param spreadsheetTitle
 * @param userAuthStatus
 * @param userAuthHelpMessage
 */
function setActionBarContents(
    selectedProfileId,
    validProfiles,
    spreadsheetTitle,
    userAuthStatus,
    userAuthHelpMessage
) {
  STORE.selectedProfileId = selectedProfileId;
  STORE.validProfiles = validProfiles;
  STORE.spreadsheetTitle = spreadsheetTitle;
  STORE.userAuthStatus = userAuthStatus;
  STORE.userAuthHelpMessage = userAuthHelpMessage;
}

/**
 * Shows the error object in a modal winow.
 * @param {*} error: the error object
 * @return {void}
 */
function renderError(error) {
  const msg =
      error && error['message'] ? error['message'] : JSON.stringify(error);
  renderErrorMessage(msg);
}

/**
 * A standardised way to output errors. Outputs to console and screen.
 * @param {!string} errorMessage: The error message.
 * @param {string=} baseElementId: The Id of the DOM Element to show the error modal box.
 * @return {void}
 */
function renderErrorMessage(errorMessage, baseElementId = 'error-modal') {
  console.log(errorMessage);

  const div = document.getElementById(baseElementId);
  div.style.display = 'block';

  soy.renderElement(div, templates.errorModal, {errorMsg: errorMessage});

  const closeButton = document.getElementById('close');
  closeButton.onclick = () => (div.style.display = 'none');

  window.onclick = function (event) {
    if (event.target === div) {
      div.style.display = 'none';
    }
  };
  throw new Error(errorMessage);
}

/**
 * Display a loading colourful spinner in the provided element.
 *
 * @param {!string} outerElementId the outer element id to render the spinner.
 */
function showSpinner(outerElementId) {
  const spinnerDiv = document.getElementById(outerElementId);
  soy.renderHtml(spinnerDiv, templates.spinner());
  spinnerDiv.style.display = 'block';
}

/**
 * Handles the close button on the information Modal.
 */
function closeGenericModal() {
  document.getElementById('generic-modal').style.display = 'none';
}

/**
 * Copies the value of the provided element id to clipboard.
 * @param {!string} elementId the id of the element to select text from.
 */
function copyText(elementId) {
  const copyTextElement = document.getElementById(elementId);
  copyTextElement.select();
  document.execCommand('copy');
  displaySnackbar(`Copied the text: ${copyTextElement.value}`);
}

/**
 * Display informative message in a snackbar
 * @param {!string} message the text message to show in the snackbar.
 * @return {void}
 */
function displaySnackbar(message) {
  const snackbar = document.getElementById('snackbar');
  soy.renderElement(snackbar, templates.snackbar, {
    infoMessage: message,
  });
  snackbar.className = 'show';

  const timeout = 17000;
  //snackbar.className = 'show';
  setTimeout(() => {
    snackbar.className = snackbar.className.replace('show', 'stopshow');
  }, timeout);
  setTimeout(() => {
    snackbar.className = snackbar.className.replace('stopshow', '');
  }, timeout + 500);
}

/**
 * Disables the elements based on elementIds
 *
 * @param {!boolean} disable when true disables the UI element.
 * @param {!Array<string>} elementIds the DOM elementIds to disable
 */
function setDisabledFlagElements(disable, elementIds) {
  if (elementIds && elementIds.length > 0) {
    elementIds.forEach(
        elementId => document.getElementById(elementId).disabled = disable);
  }
}

/**
 * Sets the disabled flag as false, to enable th UI element.
 *
 * @param {...string} elementIds the DOM elementIds to enable
 * @return {void}
 */
function enableElements(...elementIds) {
  setDisabledFlagElements(false, elementIds);
}

/**
 * Sets the disabled flag as true, to disable th UI element.
 *
 * @param {...string} elementIds the DOM elementIds to enable
 * @return {void}
 */
function disableElements(...elementIds) {
  setDisabledFlagElements(true, elementIds);
}

/**
 * Returns the text value of a text-box and trims if non-null.
 *
 * @param elementId {!string} the Element ID of the text-box
 * @return {?string}
 */
function retrieveAndTrimText(elementId) {
  const value = document.getElementById(elementId).value;
  return (value) ? value.trim() : null;
}

exports = {
  Routes,
  STORE,
  FLOODY_API_ENDPOINT,
  throwEvent,
  checkAndUpgradeSsl,
  getProfileId,
  floodyGetConfig,
  getFloodyFiles,
  renderError,
  renderErrorMessage,
  StoreType,
  setDefaultAccountStoreProp,
  setDefaultFloodlightStoreProp,
  loadHeaderBar,
  setActionBarContents,
  copyText,
  displaySnackbar,
  displaySpinner: showSpinner,
  enableElements,
  disableElements,
  retrieveAndTrimText,
};

/** Export symbols to prevent minification */
window['throwEvent'] = throwEvent;
window['handleClientLoad'] = handleClientLoad;
window['handleSignInClick'] = handleSignInClick;
window['handleSignOutClick'] = handleSignOutClick;
window['closeGenericModal'] = closeGenericModal;
window['copyText'] = copyText;
window['displaySnackbar'] = displaySnackbar;
window['loadHeaderBar'] = loadHeaderBar;
window['checkAndUpgradeSsl'] = checkAndUpgradeSsl;

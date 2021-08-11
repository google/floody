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

goog.module('manageUi.logic');

const common = goog.require('common.logic');
const soy = goog.require('goog.soy');
const templatesFloody = goog.require('floodyUi.floody');
const gtmTemplates = goog.require('floodyUi.gtm');

/**
 * Implementation of showing a snackbar on screen.
 * @type {function(!string):void}
 */
const displaySnackbar = common.displaySnackbar;

/**
 * Alias to retrieve text value from a text box.
 *
 * @type {function(!string): ?string}
 */
const retrieveAndTrimText = common.retrieveAndTrimText;

/**
 * Alias to disable UI elements
 *
 * @type {function(...string): void}
 */
const disableElements = common.disableElements;

/**
 * Implementation of showing an error message in a modal window.
 * @type {function(*):void}
 */
const renderError = common.renderError;

/**
 * Implementation of showing an error message in a modal window.
 * @type {function(!string, string=):void}
 */
const renderErrorMessage = common.renderErrorMessage;

/**
 * Show error modal with predefined error message for Invalid Profile Id.
 * @type {function():void}
 */
const renderNoProfileError = () =>
    renderErrorMessage(
        'No valid profile ID selected. Please select a profile ID and try again'
    );

/**
 * Show error modal with predefined error message for Invalid SheetId.
 * @type {function():void}
 */
const renderInvalidSheetError = () =>
    renderErrorMessage('Invalid Sheet Id selected.');

/** Parse the spreadsheet id by parsing the URL. */
const getSpreadsheetId = () =>
    new URLSearchParams(window.location.search).get('id');

const hideGenericModal = () =>
    (document.getElementById('generic-modal').style.display = 'none');

const showTempalteInGenericModal = (soyTemplate, templateParams = {}) => {
  const modalDiv = document.getElementById('generic-modal');

  // Make the Modal window visible
  modalDiv.style.display = 'block';

  soy.renderElement(modalDiv, soyTemplate, templateParams);
};

/**
 * Loads the generated floodlight spreadsheet.
 */
function loadSheet() {
  document.activeElement.blur();
  const sheetId = getSpreadsheetId();
  const sheetsFrame = document.getElementById('sheets-frame');
  const expandButton = document.getElementById('expand-button');
  const setSheetsFrameSize = () =>
      sheetsFrame.setAttribute('height', window.innerHeight - 165);

  if (!sheetId) {
    renderErrorMessage(
        `Pass the spreadsheet Id in the Url as query parameter (sheetId). ${window.location.origin}${window.location.pathname}?id=YOUR_SPREADSHEET_ID`,
        'sheets-frame'
    );
  }

  checkUserAuth();
  sheetsFrame.setAttribute(
      'src',
      `https://docs.google.com/spreadsheets/d/${sheetId}/edit?rm=minimal`
  );
  expandButton.setAttribute(
      'href',
      `https://docs.google.com/spreadsheets/d/${sheetId}`
  );

  setSheetsFrameSize();
  window.onresize = setSheetsFrameSize;
}

/**
 * Returns an array of values by splitting text-area or text-box with space or comma.
 *
 * @param elementId {!string} the text element to pull values
 * @return {!Array<string>}
 */
function retrieveAndSplitArea(elementId) {
  const value = document.getElementById(elementId).value;
  return value ? value.split(/[\n,]/g).map(s => s.trim()) : [];
}

/**
 * Send the request to share the spreadsheet
 */
function shareSpreadsheet() {
  const isEmptyArray = item =>
      item != null && item.length === 1 && item[0] === '';

  const users = retrieveAndSplitArea('share-users-area');
  const groups = retrieveAndSplitArea('share-groups-area');

  const shareRequest = {};

  if (!isEmptyArray(users)) {
    shareRequest['users'] = users;
  }

  if (!isEmptyArray(groups)) {
    shareRequest['groups'] = groups;
  }

  const sheetId = getSpreadsheetId();

  if (!sheetId) {
    renderInvalidSheetError();
  }

  hideGenericModal();

  const fetchConfig = common.floodyGetConfig();
  fetchConfig['method'] = 'POST';
  fetchConfig['body'] = JSON.stringify(shareRequest);
  fetchConfig['headers']['Content-Type'] = 'application/json';

  fetch(`${common.FLOODY_API_ENDPOINT}/admin/share/${sheetId}`, fetchConfig)
  .then(() => displaySnackbar('Sharing...'))
  .catch(error => renderError(error));
}

/**
 * Displays Share box to user.
 */
const showShareDialog = () =>
    showTempalteInGenericModal(templatesFloody.shareSheetDialog);

/**
 * Displays GTM User Input Box to user.
 */
const showGTMUserInput = () =>
    showTempalteInGenericModal(gtmTemplates.GTMUserInput);

/**
 * On clicking submit on the GTM user input screen.
 */
function submitGTM() {
  const sheetId = getSpreadsheetId();

  if (!sheetId) {
    renderError('Invalid Sheet Id');
  }

  const gtmContainerId = retrieveAndTrimText('gtm-container-id');
  const requesterDetails = retrieveAndTrimText('gtm-requester-message-id');
  const approverEmails = retrieveAndSplitArea('gtm-approver-emails-id');

  const fetchConfig = common.floodyGetConfig();
  fetchConfig['method'] = 'POST';
  fetchConfig['body'] = JSON.stringify({
    spreadsheetId: `${sheetId}`,
    gtmContainerId: `${gtmContainerId}`,
    requesterMessage: `${requesterDetails}`,
    approverEmails: approverEmails,
  });
  fetchConfig['headers']['Content-Type'] = 'application/json';

  // disable controls.
  disableElements(
      'gtm-container-id',
      'gtm-requester-message-id',
      'gtm-approver-emails-id',
      'close',
      'submit');

  // show a spinner in interim.
  common.displaySpinner('gtm-request-spinner');

  const hostname = window.location.origin;
  fetch(`${common.FLOODY_API_ENDPOINT}/gtmrequest/create`, fetchConfig)
  .then(response => response.json())
  .then(response => {
    showTempalteInGenericModal(gtmTemplates.GTMInfoBox, {
      response: response,
      hostname: hostname,
    });
  })
  .catch(error => renderError(error));
}

/**
 * Checks user's authorization to enable/disable functionalities.
 *
 * It also loads the spreadsheet title and user's DCM profiles usable for the
 * given spreadsheet.
 */
function checkUserAuth() {
  const sheetId = getSpreadsheetId();
  fetch(
      `${common.FLOODY_API_ENDPOINT}/user/checkUserAuth/${sheetId}`,
      common.floodyGetConfig()
  )
  .then(response => response.json())
  .then(authResponse => {
    const selectedProfileId = (validProfiles => {
      return validProfiles && validProfiles.length > 0
          ? `${validProfiles[0]['id']}`
          : null;
    })(authResponse['userDcmProfiles']);

    window.sessionStorage.setItem('profileId', selectedProfileId);

    common.setActionBarContents(
        selectedProfileId,
        authResponse['userDcmProfiles'],
        authResponse['spreadsheetInformation']['name'],
        authResponse['status'],
        getAuthHelpMessage(authResponse['status'])
    );

    common.loadHeaderBar('manage');
  })
  .catch(error => renderError(error));
}

/**
 * Returns correct Auth Message.
 * @param {string} authStatus: The auth status returned by floody API.
 * @return {string}
 */
function getAuthHelpMessage(authStatus) {
  switch (authStatus) {
    case 'NO_AUTH':
      return "Selected Google account doesn't have CM and Spreadsheet access.";
    case 'DCM_ONLY_AUTH':
      return 'Selected Google account does not have access to this Spreadsheet.';
    case 'SHEET_ONLY_AUTH':
      return 'Selected Google account does not have a User Profile in the linked CM account.';
    default:
      return '';
  }
}

/**
 * Export to CM
 */
function exportToDcm() {
  console.log('exportToDCM');
  const sheetId = getSpreadsheetId();

  const selectedProfileId = common.getProfileId();

  if (!selectedProfileId) {
    renderNoProfileError();
  }

  displaySnackbar('Exporting...');
  fetch(
      `${common.FLOODY_API_ENDPOINT}/floody/exportToDcm/${sheetId}`,
      common.floodyGetConfig()
  )
  .then(response => response.text())
  .catch(error => renderError(error));
}

/** Import from CM */
function importFromDcm() {
  console.log('ImportFromDcm');
  const sheetId = getSpreadsheetId();
  const selectedProfileId = common.getProfileId();

  if (!selectedProfileId) {
    renderNoProfileError();
  }

  displaySnackbar('Importing...');
  fetch(
      `${common.FLOODY_API_ENDPOINT}/floody/exportToSheet/${sheetId}`,
      common.floodyGetConfig()
  )
  .then(response => response.text())
  .catch(error => renderError(error));
}

/**
 * Adds 100 lines to the spreadsheet.
 */
function addRows() {
  const sheetId = getSpreadsheetId();
  displaySnackbar('Adding rows...');
  fetch(
      `${common.FLOODY_API_ENDPOINT}/admin/addRows/${sheetId}`,
      common.floodyGetConfig()
  )
  .then(() =>
      displaySnackbar('Successfully added 100 rows to the spreadsheet.')
  )
  .catch(error => renderError(error));
}

exports = [
  loadSheet,
  exportToDcm,
  importFromDcm,
  addRows,
  shareSpreadsheet,
  showShareDialog,
  showGTMUserInput,
  submitGTM,
];

/** Export symbols to prevent minification */
window['loadSheet'] = loadSheet;
window['exportToDcm'] = exportToDcm;
window['importFromDcm'] = importFromDcm;
window['addRows'] = addRows;
window['shareSpreadsheet'] = shareSpreadsheet;
window['showShareDialog'] = showShareDialog;
window['showGTMUserInput'] = showGTMUserInput;
window['submitGTM'] = submitGTM;

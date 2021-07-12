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

goog.module('floodyUi.logic');

const safe = goog.require('goog.dom.safe');
const soy = goog.require('goog.soy');
const common = goog.require('common.logic');
const templates = goog.require('floodyUi.floody');
const templates_fs = goog.require('floodyUi.fileSelector');

/**
 * Common Store which provides local storage implementation.
 *
 * @type {!common.StoreType}
 */
const STORE = common.STORE;

/** Google Docs Configuration */
const FLOODY_MANAGE_URL = 'manage.html?id=';

/**
 * Handles the creation of a new Floody instance.
 */
function handleCreateNew() {
  STORE.setRoute(common.Routes.CREATENEW);
  loadProfiles();
}

/**
 * Implementation of showing an error in a modal window.
 * @type {function(*):void}
 */
const renderError = common.renderError;

/**
 * Makes a call to the Floody API to retrieve the list of profiles for the
 * current user.
 */
function loadProfiles() {
  fetch(common.FLOODY_API_ENDPOINT + '/user/profiles', common.floodyGetConfig())
    .then(response => response.json())
    .then(responseJson => {
      if (!responseJson['items']) {
        throw new Error(
          'Empty Profile: User does not have a Campaign Manager profile.'
        );
      }

      return responseJson;
    })
    .then(dcmObjectList => {
      STORE.profiles = dcmObjectList['items'];
      render();
    })
    .catch(error => renderError(error));
}

/**
 * Loads accounts based on a provided profileId.
 * Should be triggered when the selected profile changes.
 * @param {string} profileId: The selected profile ID.
 */
function handleProfileSelection(profileId) {
  STORE.selectedProfile = profileId;
  window.sessionStorage.setItem('profileId', profileId);
  common.setDefaultAccountStoreProp();
  common.setDefaultFloodlightStoreProp();
  render(loadAccounts);
}

/**
 * Sets the selected floodlight configuration id in the Store.
 * Should be triggered when the selected floodlight configuration changes.
 * @param {string} floodlightConfigId: The selected floodlight config ID.
 */
function handleFloodlightConfigSelection(floodlightConfigId) {
  STORE.selectedFloodlightConfig = floodlightConfigId;
  render();
}

/**
 * Loads accounts from the Floody API.
 */
function loadAccounts() {
  if (STORE.selectedProfile) {
    const floodyAccounts =
      common.FLOODY_API_ENDPOINT + '/user/accounts/' + STORE.selectedProfile;
    fetch(floodyAccounts, common.floodyGetConfig())
      .then(response => response.json())
      .then(dcmObjectList => {
        STORE.accounts = dcmObjectList['items'];
        console.log(STORE.accounts);
        if (STORE.accounts.length != 0) {
          STORE.selectedAccount =
            /** @type {{id:number}}*/
            (STORE.accounts[0]).id.toString();
        }
        return true;
      })
      .then(() => {
        if (STORE.selectedAccount) {
          render(loadFloodlightConfigs);
        } else {
          render();
        }
      })
      .catch(error => renderError(error));
  }
}

/**
 * Loads floodlight configurations from the Floody API.
 */
function loadFloodlightConfigs() {
  if (STORE.selectedProfile && STORE.selectedAccount) {
    const floodyConfigsEndpoint = `${common.FLOODY_API_ENDPOINT}/user/floodlightconfigs/${STORE.selectedProfile}?accountId=${STORE.selectedAccount}`;
    fetch(floodyConfigsEndpoint, common.floodyGetConfig())
      .then(response => response.json())
      .then(dcmObjectList => {
        STORE.floodlightConfigs = dcmObjectList['items'];
        return true;
      })
      .then(() => render())
      .catch(error => renderError(error));
  }
}

/**
 * Validates the selected form contents. If they are valid, create the floody
 * instance.
 */
function validateFormContents() {
  if (STORE.storeContentsAreValid()) {
    createFloodyInstance();
  } else {
    STORE.errorMessage = 'Please ensure all values are filled.';
    render();
  }
}

/**
 * Creates the floody instance by sending a POST request to the Floody API.
 */
function createFloodyInstance() {
  STORE.setRoute(common.Routes.LOADING);
  const sheetInitUrl = `${common.FLOODY_API_ENDPOINT}/admin/init/${STORE.selectedAccount}/${STORE.selectedFloodlightConfig}`;
  fetch(sheetInitUrl, common.floodyGetConfig())
    .then(response => response.json())
    .then(floodySheet =>
      safe.setLocationHref(
        window.location,
        `${FLOODY_MANAGE_URL}${floodySheet['id']}`
      )
    )
    .catch(error => renderError(error));
}

/**
 * The primary render function. Controls the contents of the form.
 * @param {?Function=} callback: The callback function.
 */
function render(callback = null) {
  console.log(`Rendering for the route: ${STORE.route}`);

  if (document.hasFocus()) {
    document.activeElement.blur();
  }

  common.loadHeaderBar('default');

  if (STORE.isSignedIn) {
    const userdetail = document.getElementById('user-details');
    /** @type {function((!Object|null),
     * (!soy.IjData|!Object<string, *>|null)=): *} */
    const userTemplate = templates.userdetails;
    soy.renderElement(userdetail, userTemplate, {
      email: STORE.email,
      profilePicture: STORE.profilePicture,
    });
  }

  const div = document.getElementById('floody');

  switch (STORE.route) {
    case common.Routes.HOMEPAGE:
      soy.renderElement(div, templates.welcomePage, {});
      break;

    case common.Routes.FILESELECT:
      common.displaySpinner('floody');
      common.getFloodyFiles().then(recentSheets =>
        soy.renderElement(
          document.getElementById('floody'),
          templates_fs.fileSelector,
          {
            floodyManageUrl: FLOODY_MANAGE_URL,
            files: recentSheets,
            eventName: 'create-new-floody-instance',
          }
        )
      );
      break;

    case common.Routes.CREATENEW:
      const formTemplate = /** @type {function((!Object|null),
       * (!Object<string,*>|!soy.IjData|null)=): *} */ (templates.form);
      soy.renderElement(div, formTemplate, {
        email: STORE.email,
        selectedProfile: STORE.selectedProfile,
        profiles: STORE.profiles,
        selectedAccount: STORE.selectedAccount,
        accounts: STORE.accounts,
        selectedFloodlightConfig: STORE.selectedFloodlightConfig,
        floodlightConfigs: STORE.floodlightConfigs,
        errorMessage: STORE.errorMessage,
      });
      break;

    case common.Routes.LOADING:
      common.displaySpinner('floody');
      break;
  }

  if (callback) {
    callback();
  }
}

/**
 * Displays the Disclaimer Box.
 */
function openDisclaimerBox() {
  const div = document.getElementById('prod-counsel');
  document.getElementById('close').onclick = () => {
    div.style.display = 'none';
  };
  div.style.display = 'block';
}

/**
 * Determine whether the disclaimerExpiry has expired.
 * @return {boolean} Returns true if dislcaimerExpiry key-value pair has expired.
 */
function isDisclaimerExpired() {
  return (
    !window.localStorage ||
    Number(window.localStorage.getItem('ProductCounselDisclaimerExpiry')) <
      new Date().getTime()
  );
}

/**
 * Sets the disclaimerExpiry key-value pair in the local storage of the user's
 * browser.
 */
function setDisclaimerExpiry() {
  const buildExpiry = () => {
    const newExpiry = new Date();
    newExpiry.setHours(newExpiry.getHours() + 24);
    return newExpiry.getTime();
  };

  window.localStorage.setItem('ProductCounselDisclaimerExpiry', buildExpiry());
}

/**
 * Shows the Disclaimer message to the user if the key-value is expired or not
 * present.
 */
function showDisclaimerIfExpired() {
  if (isDisclaimerExpired()) {
    openDisclaimerBox();
    setDisclaimerExpiry();
  }
}

/**
 * Register all event listners on the main page.
 */
function registerEventListners() {
  window.addEventListener(
    'display-product-counsel-message',
    () => showDisclaimerIfExpired(),
    false
  );
  window.addEventListener('route-updated', () => render());
  window.addEventListener(
    'create-new-floody-instance',
    () => handleCreateNew(),
    false
  );
}

exports = [
  handleCreateNew,
  handleProfileSelection,
  handleFloodlightConfigSelection,
  createFloodyInstance,
  validateFormContents,
  registerEventListners,
  render,
];

/** Export symbols to prevent minification */
window['handleCreateNew'] = handleCreateNew;
window['handleProfileSelection'] = handleProfileSelection;
window['handleFloodlightConfigSelection'] = handleFloodlightConfigSelection;
window['createFloodyInstance'] = createFloodyInstance;
window['validateFormContents'] = validateFormContents;
window['registerEventListners'] = registerEventListners;
window['render'] = render;

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

goog.module('gtmUi.logic');

const common = goog.require('common.logic');
const soy = goog.require('goog.soy');
const gtmTemplates = goog.require('floodyUi.gtm');

/**
 * Implementation of showing a snackbar on screen.
 * @type {function(!string):void}
 */
const displaySnackbar = common.displaySnackbar;

/**
 * Implementation of showing a spinner on screen.*
 * @type {function(!string):void}
 */
const displaySpinner = common.displaySpinner;

/**
 * Implementation of showing an error message in a modal window.
 * @type {function(*):void}
 */
const renderError = common.renderError;

/**
 * A function to display gtm request
 * @param {!string} requestId: the ID of the GTM request to be fetched
 */
function displayGtmRequest(requestId) {
  common.loadHeaderBar('gtm');
  const fetchConfig = common.floodyGetConfig();
  fetch(`${common.FLOODY_API_ENDPOINT}/gtmrequest/${requestId}`, fetchConfig)
    .then(response => response.json())
    .then(response => {
      console.log(response);
      const templateParameters = {
        requestId: response['id'],
        requesterEmail: response['requesterEmail'],
        gtmContainerId: response['gtmContainerId'],
        floodlightActivities: response['floodlightActivities'],
      };

      if (response['actionInformation']) {
        templateParameters['authorizer'] =
          response['actionInformation']['authorizer'];
        templateParameters['action'] = response['actionInformation']['action'];
        templateParameters['comment'] =
          response['actionInformation']['comment'];
        templateParameters['timestamp'] = new Date(
          response['actionInformation']['timestamp']
        ).toString();
        templateParameters['gtmTagOperationResults'] =
          response['gtmTagOperationResults'];
      }

      soy.renderElement(
        document.getElementById('gtm-request'),
        gtmTemplates.requestDisplay,
        templateParameters
      );

      soy.renderElement(
        document.getElementById('gtm-header'),
        gtmTemplates.gtmBar,
        {
          gtmContainerId: response.gtmContainerId,
          requesterEmail: response.requesterEmail,
          requestId: response.id,
        }
      );
    })
    .catch(error => renderError(error));
}

/**
 * Sends an approve or reject action for the loaded GtmExportRequest.
 *
 * @param {!number} requestId: The GtmExport request id to take action on.
 * @param {!string} action: the GTM action
 */
function sendGtmAction(requestId, action) {
  const approveButton = document.getElementById('gtm-request-action-approve');
  const rejectButton = document.getElementById('gtm-request-action-reject');
  const commentTextbox = document.getElementById('gtm-action-comment');
  const spinner = document.getElementById('gtm-request-action-spinner');
  const actionDiv = document.getElementById('gtm-action');

  const approverComment = commentTextbox.value;
  if (action === 'reject' && !approverComment) {
    displaySnackbar('Comment is required for Rejection action');
    return;
  }

  displaySpinner('gtm-request-action-spinner');

  approveButton.disabled = true;
  rejectButton.disabled = true;
  commentTextbox.disabled = true;

  const fetchConfig = common.floodyGetConfig();
  fetchConfig['method'] = 'POST';
  fetchConfig['body'] = approverComment;
  fetchConfig['headers']['Content-Type'] = 'application/json';

  fetch(
    `${common.FLOODY_API_ENDPOINT}/gtmrequest/${requestId}:${action}`,
    fetchConfig
  )
    .then(response => response.json())
    .then(response => {
      spinner.style.display = 'none';
      actionDiv.style.display = 'none';

      const templateParameters = {
        action: response['action'],
      };

      if (response['gtmTagOperationResult']) {
        templateParameters['gtmTagOperationResults'] =
          response['gtmTagOperationResult'];
      }

      soy.renderElement(
        document.getElementById('gtm-tag-operation-results'),
        gtmTemplates.gtmTagOperationResultsDisplay,
        templateParameters
      );
    })
    .catch(error => console.log(error));
}

exports = [displayGtmRequest, sendGtmAction];

/** Export symbols to prevent minification */
window['displayGtmRequest'] = displayGtmRequest;
window['sendGtmAction'] = sendGtmAction;

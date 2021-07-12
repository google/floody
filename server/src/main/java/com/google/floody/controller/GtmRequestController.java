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

package com.google.floody.controller;

import static com.google.common.base.Preconditions.checkState;
import static com.google.floody.service.DatastoreService.ofy;

import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.exceptions.UnauthorizedUserException;
import com.google.floody.model.GtmExport;
import com.google.floody.model.TagOperationResult;
import com.google.floody.protobuf.GtmOperations.GtmExportRequest;
import com.google.floody.protobuf.GtmOperations.GtmExportResponse;
import com.google.floody.protobuf.GtmOperations.GtmOperationStatus;
import com.google.floody.protobuf.GtmOperations.GtmTagOperationAction;
import com.google.floody.protobuf.GtmOperations.GtmTagOperationResult;
import com.google.floody.protobuf.GtmOperations.GtmTagOperationResults;
import com.google.floody.service.ObjectifySaverService;
import com.google.floody.spreadsheet.GoogleSpreadsheetService;
import com.googlecode.objectify.NotFoundException;
import java.io.IOException;
import java.util.HashSet;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provides end-points for GTM requests management. */
@RestController
@RequestMapping("/gtmrequest")
public final class GtmRequestController extends FloodyBaseController {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @PostMapping("/create")
  public GtmExportResponse createGtmRequest(@RequestBody GtmExportRequest gtmOperationRequest) {

    try {
      checkUserFullAuth(gtmOperationRequest.getSpreadsheetId());

      String requesterEmail = userServicesFactory().getAccountEmail();

      GoogleSpreadsheetService spreadsheetService =
          robotServicesFactory()
              .buildSpreadsheetFactory()
              .forSpreadsheet(gtmOperationRequest.getSpreadsheetId());

      // Read Updatable Floodlights from Spreadsheet
      var saverService = new ObjectifySaverService<GtmExport>(ofy());

      robotServicesFactory()
          .buildFloodyService()
          .readFromSheets()
          .forSpreadsheet(gtmOperationRequest.getSpreadsheetId())
          .load()
          .toGtmRequestWriter(saverService)
          .forRequest(gtmOperationRequest, requesterEmail)
          .sync()
          .toSheets(spreadsheetService)
          .sync();

      var gtmExport = saverService.getSavedObjects().get(0);

      return GtmExportResponse.newBuilder()
          .setRequest(gtmOperationRequest)
          .setRequestId(gtmExport.getId())
          .setRequestUri("/gtm.html?id=" + gtmExport.getId())
          .setStatus(GtmOperationStatus.newBuilder().setSuccess(true))
          .build();
    } catch (IOException ioexp) {
      logger.atWarning().withStackTrace(StackSize.MEDIUM).withCause(ioexp).log(
          "Error processing gtmRequest:\n%s", gtmOperationRequest);
      return GtmExportResponse.newBuilder()
          .setRequest(gtmOperationRequest)
          .setStatus(
              GtmOperationStatus.newBuilder().setSuccess(false).setErrorMessage(ioexp.getMessage()))
          .build();
    }
  }

  @GetMapping("/{requestId:\\d+}")
  public GtmExport retrieveGtmRequest(
      @PathVariable("requestId") long requestId, HttpServletResponse response) throws IOException {
    try {
      return loadGtmRequest(requestId, userServicesFactory().getAccountEmail());
    } catch (NotFoundException noFoundException) {
      response.setStatus(404);
      return null;
    }
  }

  @PostMapping("/{requestId:\\d+}:approve")
  public GtmTagOperationResults approveGtmRequest(
      @PathVariable("requestId") long requestId,
      @RequestBody(required = false) String authorizerComment) {
    try {
      var userEmail = userServicesFactory().getAccountEmail();
      var gtmExport = loadGtmRequestAndCheckUnActioned(requestId, userEmail);

      var tagOperationResultSet = new HashSet<TagOperationResult>();

      checkState(
          gtmExport.getFloodlightActivities() != null
              && gtmExport.getFloodlightActivities().size() > 0);
      var tagOperationResults =
          userServicesFactory()
              .buildGtmServiceFor(gtmExport.getGtmContainerId())
              .insertTagsFor(gtmExport)
              .execute();

      for (GtmTagOperationResult result : tagOperationResults) {
        TagOperationResult.Builder tagOperationResultBuilder = new TagOperationResult.Builder();
        tagOperationResultSet.add(
            tagOperationResultBuilder
                .setFloodlightActivityName(result.getFloodlightActivity())
                .setMessage(result.getMessage())
                .setSuccess(result.getSuccess())
                .build());
      }
      var updated = gtmExport.toBuilder().setGtmTagOperationResults(tagOperationResultSet).build();
      ofy().save().entity(updated.withApprovalNow(userEmail, authorizerComment)).now();

      return GtmTagOperationResults.newBuilder()
          .addAllGtmTagOperationResult(tagOperationResults)
          .setAction(GtmTagOperationAction.APPROVE)
          .setSuccess(true)
          .build();

    } catch (IOException ioexception) {
      logger.atWarning().log(
          "TagManager API Error: (%s) while processing request (%s)",
          ioexception.getMessage(), requestId);
      return GtmTagOperationResults.newBuilder().setSuccess(false).build();
    }
  }

  @PostMapping("/{requestId:\\d+}:reject")
  public GtmTagOperationResults rejectGtmRequest(
      @PathVariable("requestId") long requestId, @RequestBody String authorizerComment) {
    try {
      var userEmail = userServicesFactory().getAccountEmail();
      var gtmExport = loadGtmRequestAndCheckUnActioned(requestId, userEmail);

      ofy().save().entity(gtmExport.withRejectionNow(userEmail, authorizerComment)).now();

      return GtmTagOperationResults.newBuilder()
          .setAction(GtmTagOperationAction.REJECT)
          .setSuccess(true)
          .build();
    } catch (IOException ioexception) {
      return GtmTagOperationResults.newBuilder().setSuccess(false).build();
    }
  }

  private void checkUserFullAuth(String spreadsheetId) throws IOException {
    if (!robotServicesFactory()
        .buildUiUserAuthService(spreadsheetId, userServicesFactory())
        .isFullAuth()) {
      throw new UnauthorizedUserException("User does not have full-auth");
    }
  }

  private GtmExport loadGtmRequest(long requestId, String userEmail) {
    var gtmRequest = ofy().load().type(GtmExport.class).id(requestId).safe();

    // Check if requester email
    boolean isUserAuthorizedUser =
        userEmail.equals(gtmRequest.getRequesterEmail())
            // Check if approverEmail
            || gtmRequest.getApproverEmails().contains(userEmail);

    if (!isUserAuthorizedUser) {
      throw new UnauthorizedUserException("User UnAuthorized for this Action");
    }

    return gtmRequest;
  }

  private GtmExport loadGtmRequestAndCheckUnActioned(long requestId, String userEmail) {
    var gtmRequest = loadGtmRequest(requestId, userEmail);

    if (gtmRequest.getActionInformation() != null) {
      throw new IllegalArgumentException(
          "GtmRequest ("
              + requestId
              + ") is already actioned ("
              + gtmRequest.getActionInformation().getAction()
              + ")");
    }

    return gtmRequest;
  }
}

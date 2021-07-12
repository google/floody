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

import static com.google.floody.service.ServicesFactory.buildRobotsServicesFactory;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.floody.exceptions.UnauthorizedUserException;
import com.google.floody.model.FloodyProperties;
import com.google.floody.protobuf.UserOperations.HeartBeat;
import com.google.floody.service.ServicesFactory;
import com.google.protobuf.util.Timestamps;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class FloodyBaseController {

  @Autowired private HttpServletRequest activeRequest;

  @Autowired private FloodyProperties floodyProperties;

  protected ServicesFactory robotServicesFactory() {
    return buildRobotsServicesFactory(floodyProperties);
  }

  /** Echos the token back as an heartbeat. */
  @GetMapping("/heart")
  public HeartBeat heartBeat(
      @RequestHeader(required = false) String token,
      @RequestHeader(required = false, name = "Authorization") String authorization) {

    var userToken = getUserAuthToken();

    var heartBeatBuilder =
        HeartBeat.newBuilder()
            .setTimestamp(Timestamps.fromSeconds(ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()))
            .setUserToken(userToken);

    if (!isBlank(userToken)) {
      try {
        heartBeatBuilder.setTokenInfo(
            userServicesFactory().buildTokenInfoService().retrieveTokenInfo().toPrettyString());
      } catch (IOException exception) {
        heartBeatBuilder.setTokenInfo("error retrieving token information");
      }
    }

    return heartBeatBuilder.build();
  }

  protected ServicesFactory userServicesFactory() {
    if (activeRequest == null) {
      return null;
    }

    Object rawUsersServiceFactory = activeRequest.getAttribute("userServicesFactory");

    if (!(rawUsersServiceFactory instanceof ServicesFactory)) {
      throw new IllegalStateException("userServicesFactory not found or not of correct type.");
    }

    return (ServicesFactory) rawUsersServiceFactory;
  }

  private String getUserAuthToken() {
    if (activeRequest == null) {
      return StringUtils.EMPTY;
    }

    var authHeader = activeRequest.getHeader("Authorization");

    return (authHeader != null) ? authHeader : StringUtils.EMPTY;
  }

  protected void verifyUserHasFullAuthOrReturn(String spreadsheetId) throws IOException {

    var uiAuthService =
        robotServicesFactory().buildUiUserAuthService(spreadsheetId, userServicesFactory());

    if (!uiAuthService.isFullAuth()) {
      throw new UnauthorizedUserException(spreadsheetId);
    }
  }

  protected FloodyBaseController() {}
}

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

import com.google.common.flogger.GoogleLogger;
import com.google.floody.exceptions.UnauthorizedUserException;
import com.google.floody.model.FloodyProperties;
import com.google.floody.protobuf.FileOperations.FileOperationResultList;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Provides end-points for all cron tasks. */
@RestController
@RequestMapping("/crontasks")
public final class CronTasksController extends FloodyBaseController {

  private static GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Autowired private FloodyProperties floodyProperties;

  @GetMapping(value = "/removeOldFiles")
  public FileOperationResultList deleteOlderFiles(
      @RequestParam(required = false, defaultValue = "false") boolean dryRun)
      throws IOException, InterruptedException {

    verifyCaller();

    var cutOffDate =
        ZonedDateTime.now(ZoneOffset.UTC).minusDays(floodyProperties.getGeneratedFileTtlDays());

    return robotServicesFactory().buildFileDeletor(dryRun).deleteFilesLastAccessed(cutOffDate);
  }

  private void verifyCaller() {
    try {
      var validUser =
          userServicesFactory()
              .getIdTokenVerifier()
              .retrieveEmailAddress()
              .get()
              .equals(robotServicesFactory().getAccountEmail());

      if (validUser) {
        return;
      }
    } catch (Exception exception) {
      logger.atInfo().log("error verifying caller: %s", exception.getMessage());
    }

    throw new UnauthorizedUserException("Unknown caller for secured endpoint");
  }
}

#!/bin/bash
#
# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Google Cloud project id
export PROJECT_ID="[google-cloud-project-id]"

# The Google Cloud region to deploy Cloud Run instances
export REGION_ID="[compute-engine-region]"

# Name of the service account to use (not the email address)
export FLOODY_SERVICE_ACCOUNT_NAME="floody-application-service"

# Google Cloud IAM Role name for Floody service account
export FLOODY_ROLE_NAME="floody_application_role"



# Running locally
export GOOGLE_APPLICATION_CREDENTIALS="[path/to/service/account/credential/file.json]"
export DATASTORE_EMULATOR_HOST="localhost:8081"
export DATASTORE_PROJECT_ID="${PROJECT_ID}"
export EXECUTION_ENVIRONMENT="local"
export FLOODY_CLIENT_ID="[the-local-testing-client-id]"

################################################
##      DO NOT EDIT BELOW THIS LINE           ##
################################################
FLOODY_SERVICE_ACCOUNT_EMAIL="${FLOODY_SERVICE_ACCOUNT_NAME}@$(echo $PROJECT_ID | awk -F':' '{print $2"."$1}' | sed 's/^\.//').iam.gserviceaccount.com"
export FLOODY_SERVICE_ACCOUNT_EMAIL



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

package com.google.floody.model;

import com.google.floody.protobuf.UserOperations.DcmObject;
import com.google.floody.protobuf.UserOperations.DcmObject.DcmObjectType;

/** Static factory to instantiate Data Exchange Object to share through UserAccess Service. */
public final class DcmObjects {

  /**
   * Helper Method to instantiate {@link DcmObject} with type as {@link DcmObjectType#ADVERTISER}.
   *
   * @param id DCM advertiser Id
   * @param name DCM Advertiser Name
   * @return DcmObject with id and name as specified and type as {@code DcmObjectType.ADVERTISER}
   */
  public static DcmObject createAdvertiserDcmObject(Long id, String name) {
    return DcmObject.newBuilder().setId(id).setName(name).setType(DcmObjectType.ADVERTISER).build();
  }

  /**
   * Helper Method to instantiate {@link DcmObject} with type as {@link DcmObjectType#ACCOUNT}.
   *
   * @param id DCM Network/Account Id
   * @param name DCM Network/Account Name
   * @return DcmObject with id and name as specified and type as {@code DcmObjectType.ACCOUNT}
   */
  public static DcmObject createAccountDcmObject(Long id, String name) {
    return DcmObject.newBuilder().setId(id).setName(name).setType(DcmObjectType.ACCOUNT).build();
  }

  /**
   * Helper Method to instantiate {@link DcmObject} with type as {@link DcmObjectType#USER_PROFILE}.
   *
   * @param id DCM User Profile Id
   * @param name DCM User Profile Name
   * @return DcmObject with id and name as specified and type as {@code DcmObjectType.USER_PROFILE}
   */
  public static DcmObject createUserProfileDcmObject(Long id, String name) {
    return DcmObject.newBuilder()
        .setId(id)
        .setName(name)
        .setType(DcmObjectType.USER_PROFILE)
        .build();
  }

  private DcmObjects() {}
}

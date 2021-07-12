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

package com.google.floody.transforms;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Integer.parseInt;

import com.google.api.services.tagmanager.model.Parameter;
import com.google.api.services.tagmanager.model.Tag;
import com.google.common.collect.ImmutableList;
import com.google.floody.model.GtmFloodlightActivity;

public class GtmTagTransformer {

  public static Tag transform(GtmFloodlightActivity activity, long pushRequestId) {
    Tag tag =
        new Tag()
            .setName(String.format("%s_floodyPush_%s", activity.getName(), pushRequestId))
            .setLiveOnly(false);

    // Build Parameters
    ImmutableList.Builder<Parameter> tagParameters = ImmutableList.builder();

    tagParameters.add(
        new Parameter()
            .setType("template")
            .setKey("advertiserId")
            .setValue(String.valueOf(activity.getDcmAdvertiserId())),
        new Parameter()
            .setType("template")
            .setKey("groupTag")
            .setValue(String.valueOf(activity.getType())),
        new Parameter()
            .setType("template")
            .setKey("activityTag")
            .setValue(String.valueOf(activity.getCat())));

    if (activity.getCustomVariables() != null) {
      tagParameters.add(
          new Parameter()
              .setType("list")
              .setKey("customVariable")
              .setList(
                  activity.getCustomVariables().stream()
                      .map(GtmTagTransformer::buildCustomVariableParameter)
                      .collect(toImmutableList())));
    }

    if (activity.getCountingMethod().name().startsWith("COUNTER")) {
      tag.setType("flc");

      String ordinalType = activity.getCountingMethod().name().replace("COUNTER_", "");

      tagParameters.add(
          new Parameter().setType("template").setKey("ordinalType").setValue(ordinalType));

      if (ordinalType.equals("SESSION")) {
        tagParameters.add(
            new Parameter()
                .setType("template")
                .setKey("sessionId")
                .setValue("session_information"));
      }
    } else {
      tag.setType("fls");
      String countingMethod = activity.getCountingMethod().name().replace("SALES_", "");
      countingMethod = (countingMethod.equals("ITEMS_SOLD") ? "ITEM_SOLD" : countingMethod);

      tagParameters.add(
          new Parameter().setType("template").setKey("countingMethod").setValue(countingMethod));
      tagParameters.add(
          new Parameter().setType("template").setKey("revenue").setValue("transaction_value"));
      tagParameters.add(
          new Parameter().setType("template").setKey("orderId").setValue("transaction_order_id"));

      if (countingMethod.equals("ITEM_SOLD")) {
        tagParameters.add(
            new Parameter()
                .setType("template")
                .setKey("quantity")
                .setValue("transaction_quantity"));
      }
    }

    tag.setParameter(tagParameters.build());

    return tag;
  }

  private static Parameter buildCustomVariableParameter(String uVariable) {

    var uVarNumber = parseInt(uVariable.replaceAll("^[^\\d]+", ""));

    return new Parameter()
        .setType("map")
        .setMap(
            ImmutableList.of(
                new Parameter().setType("template").setKey("key").setValue("u" + uVarNumber),
                new Parameter()
                    .setType("template")
                    .setKey("value")
                    .setValue("u" + uVarNumber + "_value")));
  }
}

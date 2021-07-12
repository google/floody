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
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.floody.spreadsheet.SheetUtils.buildAsStringList;
import static com.google.floody.spreadsheet.SheetUtils.toLongOrNull;
import static com.google.floody.spreadsheet.SheetUtils.toStringOrNull;
import static java.lang.Integer.parseInt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.flogger.StackSize;
import com.google.floody.model.ActivitySheetHeaderInformation;
import com.google.floody.model.CacheBustingType;
import com.google.floody.model.FloodlightActivityStatus;
import com.google.floody.model.SheetFloody;
import com.google.floody.model.TagFormat;
import com.google.floody.model.TagType;
import com.google.floody.spreadsheet.SpreadsheetRowTransformer;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Map a SheetFloody to a Spreadsheet row and reverse. */
public class FloodyToSpreadSheetRowTransformer implements SpreadsheetRowTransformer<SheetFloody> {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Period defaultRemarketingAudienceLifespan;

  public FloodyToSpreadSheetRowTransformer(Period defaultRemarketingAudienceLifespan) {
    this.defaultRemarketingAudienceLifespan = defaultRemarketingAudienceLifespan;
  }

  @Override
  public SheetFloody transformFromSheetRow(List<Object> values) {

    if (values == null
        || values.isEmpty()
        || values.size() < ActivitySheetHeaderInformation.CACHE_BUSTING_COLUMN) {
      return null;
    }

    try {
      SheetFloody.Builder floodyBuilder =
          SheetFloody.builder()
              .setAccountId(
                  toLongOrNull(values.get(ActivitySheetHeaderInformation.ACCOUNT_ID_COLUMN)))
              .setFloodlightConfigurationId(
                  toLongOrNull(
                      values.get(ActivitySheetHeaderInformation.FLOODLIGHT_CONFIG_ID_COLUMN)))
              .setToBeUpdated(
                  toStringOrNull(values.get(ActivitySheetHeaderInformation.FLAG_TO_UPDATE_COLUMN)))
              .setId(toLongOrNull(values.get(ActivitySheetHeaderInformation.FLOODLIGHT_ID_COLUMN)))
              .setGroupName(
                  toStringOrNull(
                      values.get(ActivitySheetHeaderInformation.ACTIVITY_GROUP_NAME_COLUMN)))
              .setGroupTagString(
                  toStringOrNull(
                      values.get(ActivitySheetHeaderInformation.GROUP_TAG_TYPE_STRING_COLUMN)))
              .setTagString(
                  toStringOrNull(
                      values.get(ActivitySheetHeaderInformation.ACTIVITY_TAG_CAT_STRING_COLUMN)))
              .setName(
                  toStringOrNull(values.get(ActivitySheetHeaderInformation.ACTIVITY_NAME_COLUMN)))
              .setCountingMethod(
                  toStringOrNull(
                      values.get(ActivitySheetHeaderInformation.COUNTING_METHODOLOGY_COLUMN)))
              .setExpectedUrl(
                  toStringOrNull(values.get(ActivitySheetHeaderInformation.EXPECTED_URL_COLUMN)));

      if (values.size() > ActivitySheetHeaderInformation.CACHE_BUSTING_COLUMN) {
        String cacheBusting =
            toStringOrNull(values.get(ActivitySheetHeaderInformation.CACHE_BUSTING_COLUMN));

        floodyBuilder.setCacheBustingMethod(CacheBustingType.valueOfOrDefault(cacheBusting));
      }

      if (values.size()
          > ActivitySheetHeaderInformation.CUSTOM_FLOODLIGHT_VARIABLES_SELECTED_COLUMN) {
        floodyBuilder.setCustomFloodlightVariables(
            buildListOfStringBySplittingOnComma(
                    toStringOrNull(
                        values.get(
                            ActivitySheetHeaderInformation
                                .CUSTOM_FLOODLIGHT_VARIABLES_SELECTED_COLUMN)))
                .stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(toImmutableSet()));
      }

      if (values.size() > ActivitySheetHeaderInformation.DEFAULT_TAGS_COLUMN) {
        floodyBuilder.setDefaultTagIds(
            buildListOfLongsBySplittingString(
                toStringOrNull(values.get(ActivitySheetHeaderInformation.DEFAULT_TAGS_COLUMN))));
      }
      if (values.size() > ActivitySheetHeaderInformation.PUBLISHER_TAGS_COLUMN) {
        floodyBuilder.setPublisherTagIds(
            buildListOfLongsBySplittingString(
                toStringOrNull(values.get(ActivitySheetHeaderInformation.PUBLISHER_TAGS_COLUMN))));
      }

      if (values.size() > ActivitySheetHeaderInformation.TAG_FORMAT_COLUMN) {
        floodyBuilder.setTagFormat(
            TagFormat.valueOfOrDefault(
                toStringOrNull(values.get(ActivitySheetHeaderInformation.TAG_FORMAT_COLUMN))));
      }
      if (values.size() > ActivitySheetHeaderInformation.TAG_TYPE_COLUMN) {
        floodyBuilder.setTagType(
            TagType.valueOfOrDefault(
                toStringOrNull(values.get(ActivitySheetHeaderInformation.TAG_TYPE_COLUMN))));
      }
      if (values.size() > ActivitySheetHeaderInformation.FLOODLIGHT_STATUS_COLUMN) {
        Object rawArchiveStatus =
            values.get(ActivitySheetHeaderInformation.FLOODLIGHT_STATUS_COLUMN);

        if (rawArchiveStatus != null && !rawArchiveStatus.equals(StringUtils.EMPTY)) {
          // Backward Compatibility
          String archiveStatus = String.valueOf(rawArchiveStatus);

          if (archiveStatus.equals("Y")) {
            floodyBuilder.setStatus(FloodlightActivityStatus.ARCHIVED_AND_DISABLED);
          } else {
            floodyBuilder.setStatus(FloodlightActivityStatus.valueOf(archiveStatus));
          }
        }
      }

      if (values.size() > ActivitySheetHeaderInformation.CREATE_AUDIENCE_COLUMN) {
        floodyBuilder.setAutoCreateAudience(
            "Y".equals(values.get(ActivitySheetHeaderInformation.CREATE_AUDIENCE_COLUMN)));
      }

      if (values.size() > ActivitySheetHeaderInformation.AUDIENCE_LIFESPAN_COLUMN) {

        var audienceLifeSpan =
            toStringOrNull(values.get(ActivitySheetHeaderInformation.AUDIENCE_LIFESPAN_COLUMN));

        floodyBuilder.setAudienceLifespan(
            (audienceLifeSpan == null)
                ? defaultRemarketingAudienceLifespan
                : Period.ofDays(parseInt(audienceLifeSpan)));
      }

      return floodyBuilder.build();
    } catch (RuntimeException exp) {
      logger.atWarning().withCause(exp).withStackTrace(StackSize.MEDIUM).log(
          "error transforming ActivityRow: " + values);
    }

    return null;
  }

  private static ImmutableList<String> buildListOfStringBySplittingOnComma(@Nullable String value) {
    if (value == null) {
      return ImmutableList.of();
    }
    return ImmutableList.copyOf(
        Splitter.on(",").trimResults().omitEmptyStrings().splitToList(value));
  }

  private static ImmutableList<Long> buildListOfLongsBySplittingString(@Nullable String value) {
    return buildListOfStringBySplittingOnComma(value).stream()
        .map(Long::valueOf)
        .collect(toImmutableList());
  }

  @Override
  public List<Object> transformToSheetRow(SheetFloody entity) {
    return buildAsStringList(
        entity.getAccountId(),
        entity.getFloodlightConfigurationId(),
        entity.getId(),
        entity.isToBeUpdated() ? "Y" : StringUtils.EMPTY,
        entity.getName(),
        entity.getTagString(),
        entity.getGroupName(),
        entity.getGroupTagString(),
        entity.getCountingMethod(),
        entity.getExpectedUrl(),
        entity.getCacheBustingMethod(),
        Joiner.on(",")
            .join(
                Optional.ofNullable(entity.getCustomFloodlightVariables())
                    .orElseGet(ImmutableSet::of)),
        Joiner.on(",")
            .join(Optional.ofNullable(entity.getDefaultTagIds()).orElseGet(ImmutableSet::of)),
        Joiner.on(",")
            .join(Optional.ofNullable(entity.getPublisherTagIds()).orElseGet(ImmutableSet::of)),
        entity.getTagFormat(),
        entity.getTagType(),
        entity.getStatus(),
        StringUtils.EMPTY,
        // represent create audience flag
        StringUtils.EMPTY, // represent audience lifespan
        entity.getRemarks());
  }
}

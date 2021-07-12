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

package com.google.floody.spreadsheet;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Integer.parseInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.api.services.sheets.v4.model.GridRange;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Collection of Utility methods for simplifying Sheets API related operations */
public final class SheetUtils {

  /** Empty String Object for better readability */
  public static final String EMPTY_STRING = "";

  private static final String LONG_PATTERN = "^\\d+$";

  private static final String RANGE_EXTRACTION_PATTERN =
      "^(?<startColumnName>[A-Z]+)?(?<startRowNumber>\\d+)?(?:\\:(?<endColumnName>[A-Z]+)?(?<endRowNumber>\\d+)?)?$";
  private static final String RANGE_VALIDATION_PATTERN =
      "^(?:[A-Z]+:[A-Z]+)$|^(?:\\d+:\\d+)$|^(?:[A-Z]+\\d+:[A-Z]+)$|^(?:[A-Z]+:[A-Z]+\\d+)$|^(?:[A-Z]+\\d+)$|^(?:[A-Z]+\\d+:[A-Z]+\\d+)$";

  /**
   * Helper function to cast the an Object to Long if not null
   *
   * @param value object to be cast
   * @return Long value or null if input value is null
   */
  public static Long toLongOrNull(Object value) {
    return Optional.ofNullable(value)
        .map(Object::toString)
        .filter(objectValue -> objectValue.matches(LONG_PATTERN))
        .map(Long::parseLong)
        .orElse(null);
  }

  /**
   * Helper function to cast an Object to String if not null
   *
   * @param value object to be cast
   * @return String value or null if input value is null
   */
  public static String toStringOrNull(Object value) {
    return Optional.ofNullable(value).map(Object::toString).orElse(null);
  }

  /**
   * Helper function to cast an list of Objects to list of String objects
   *
   * @param list of Objects to be transformed
   * @return list of String objects by using toString() on the Object
   */
  public static List<Object> convertToStringList(List<Object> list) {
    return Optional.ofNullable(list).orElseGet(Collections::emptyList).stream()
        .map(item -> (isNull(item) ? EMPTY_STRING : item.toString()))
        .collect(toImmutableList());
  }

  /**
   * Returns an ImmutableList of Strings derived by converting the parameters using toString()
   * method, or an EMPTY_STRING if the parameter is null.
   *
   * @param value object to add to the List of Strings.
   * @param otherValues objects to add to the List of Strings.
   */
  public static List<Object> buildAsStringList(Object value, Object... otherValues) {
    return Stream.concat(
            Stream.of(firstNonNull(value, EMPTY_STRING)),
            Arrays.stream(firstNonNull(otherValues, new Object[1])))
        .map(item -> (isNull(item) ? EMPTY_STRING : item.toString()))
        .collect(toImmutableList());
  }

  /**
   * Returns an ImmutableList containing the string value of the object derived by converting the
   * parameter using toString() method, or an empty List if the parameter is null.
   *
   * @param value objects to add to the List of Strings.
   */
  public static List<Object> buildAsStringList(Object value) {
    return (value == null) ? ImmutableList.of() : ImmutableList.of(value.toString());
  }

  /**
   * Helper function to transform a String Array to a Spreadsheet Row representation of {@code
   * List<List<Object>>}
   *
   * @param array String array
   * @return Spreadsheet row representation of the String array
   */
  public static List<List<Object>> transformArrayToObjectList(String... array) {

    return ImmutableList.of(ImmutableList.copyOf(array));
  }

  /**
   * Helper function to transform a List to a Spreadsheet Row representation of {@code
   * List<List<Object>>}
   *
   * <p>Uses the {@link Object#toString()} values to put in the returned list
   *
   * @param values values to be put into spreadsheet
   * @return Spreadsheet row representation of the String array
   */
  public static List<List<Object>> transformListToStringObjectList(List<?> values) {
    checkNotNull(values, "list is null");

    return ImmutableList.of(values.stream().map(Object::toString).collect(toImmutableList()));
  }

  /**
   * Builds a complete A1 Notation range by concatenating SheetName and Range
   *
   * @param sheetName the name of the sheet
   * @return the complete A1Range including sheetName
   */
  public static String buildRangeA1NotationWithSheetName(String sheetName, String rangeA1Notation) {
    checkArgument(!isBlank(sheetName), "sheetName is empty or null");
    checkArgument(!isBlank(rangeA1Notation), "range is empty or null");
    checkArgument(
        rangeA1Notation.matches(RANGE_VALIDATION_PATTERN),
        "Incorrect A1-Range format (%s)",
        rangeA1Notation);

    String sheetNameToUse = sheetName;
    if (sheetName.contains(" ")) {
      sheetNameToUse = "'" + sheetName + "'";
    }

    return sheetNameToUse + "!" + rangeA1Notation;
  }

  /**
   * Converts the column number to the A1 notation e.g. 0 -> A, 4 -> E, 27 -> AB
   *
   * @param columnNumber of the sheet
   * @return A1 Notation of the column (e.g. 0-> A , 26 -> AB)
   */
  public static String getColumnA1Notation(int columnNumber) {
    checkArgument(columnNumber >= 0, "column number is negative");

    final StringBuilder invertedColumnNameBuilder = new StringBuilder();
    int num = columnNumber;
    while (num >= 0) {
      int numChar = (num % 26) + 'A';
      invertedColumnNameBuilder.append((char) numChar);
      num = (num / 26) - 1;
    }

    return invertedColumnNameBuilder.reverse().toString();
  }

  /**
   * Converts the A1 Column Notation to a Zero Indexed Column number e.g A -> 0, AB -> 27, Z -> 25
   *
   * @param columnName the A1notation of column e.g A, B, AA
   * @return the zero-indexed column number A->0, Z->25, AB -> 27
   */
  public static int getColumnNumberFromA1Notation(String columnName) {
    checkArgument(
        nonNull(columnName) && !isBlank(columnName), "Column Name can't be null or empty");

    int result = 0;
    for (int i = 0; i < columnName.length(); i++) {
      result *= 26;
      result += columnName.charAt(i) - 'A' + 1;
    }
    return result - 1;
  }

  /**
   * Builds a Range String in A1Notation for a given rectangle
   *
   * @param startingCell A1 notation of the starting cell
   * @return the A1Notation for the rectangle represented by the combination of starting cell and
   *     rows and columns
   */
  public static String buildRangeA1NotationForRectangle(
      String startingCell, @Nullable Integer numberOfColumns, @Nullable Integer numberOfRows) {
    checkNotNull(startingCell, "startingCell shouldn't be null");
    checkArgument(
        startingCell.matches("^[A-Z]+\\d+$"),
        "Incorrect Starting cell format (should be like C3, AB11)");
    checkArgument(
        nonNull(numberOfColumns) || nonNull(numberOfRows),
        "number of columns and rows can't be both null");

    GridRange startingCellGrid = buildGridRangeFromA1Notation(startingCell);

    GridRange rectangle = new GridRange();
    rectangle
        .setStartRowIndex(startingCellGrid.getStartRowIndex())
        .setStartColumnIndex(startingCellGrid.getStartColumnIndex());

    if (nonNull(numberOfColumns)) {
      rectangle.setEndColumnIndex(startingCellGrid.getStartColumnIndex() + numberOfColumns);
    }
    if (nonNull(numberOfRows)) {
      rectangle.setEndRowIndex(startingCellGrid.getStartRowIndex() + numberOfRows);
    }

    return buildRangeA1NotationFromGridRange(rectangle);
  }

  /**
   * Transforms {@link GridRange} object to an A1Notation Range e.g GridRange{startRowIndex=0,
   * endRowIndex=1, startColumnIndex=0, endColumnIndex=5} will be transformed as A1:F2
   *
   * @param gridRange the grid range Object to be transformed
   * @return the A1 Notation of the Range
   */
  public static String buildRangeA1NotationFromGridRange(GridRange gridRange) {
    checkNotNull(gridRange, "gridRange can't be null");
    checkArgument(
        gridRange.containsKey("startColumnIndex") || gridRange.containsKey("startRowIndex"),
        "grid range needs to have at least startRow or startColumn Index");

    StringBuilder sb = new StringBuilder();

    return sb.append(
            nonNull(gridRange.getStartColumnIndex())
                ? getColumnA1Notation(gridRange.getStartColumnIndex())
                : EMPTY_STRING)
        .append(
            nonNull(gridRange.getStartRowIndex())
                ? (gridRange.getStartRowIndex() + 1)
                : EMPTY_STRING)
        .append(
            (nonNull(gridRange.getEndRowIndex()) || nonNull(gridRange.getEndColumnIndex()))
                ? ":"
                : EMPTY_STRING)
        .append(
            nonNull(gridRange.getEndColumnIndex())
                ? getColumnA1Notation(gridRange.getEndColumnIndex() - 1)
                : EMPTY_STRING)
        .append(nonNull(gridRange.getEndRowIndex()) ? gridRange.getEndRowIndex() : EMPTY_STRING)
        .toString();
  }

  /**
   * Transforms a A1Notation Range to {@link GridRange} object e.g "A1:F2" will be transformed as
   * GridRange{startRowIndex=0, endRowIndex=1, startColumnIndex=0, endColumnIndex=5} will be
   *
   * @param a1NotationRange the range in A1Notation format
   * @return the GridRange to represent the given A1 Range
   */
  public static GridRange buildGridRangeFromA1Notation(String a1NotationRange) {
    checkNotNull(a1NotationRange, "a1 range can't be null");
    checkArgument(
        a1NotationRange.matches(RANGE_VALIDATION_PATTERN),
        "a1 notation (%s) needs to be in correct format",
        a1NotationRange);

    Matcher matcher = Pattern.compile(RANGE_EXTRACTION_PATTERN).matcher(a1NotationRange);
    matcher.find();

    String startColumnName = matcher.group("startColumnName");
    String startRowNumber = matcher.group("startRowNumber");
    String endColumnName = matcher.group("endColumnName");
    String endRowNumber = matcher.group("endRowNumber");

    return new GridRange()
        .setStartColumnIndex(
            nonNull(startColumnName) ? getColumnNumberFromA1Notation(startColumnName) : null)
        .setStartRowIndex(nonNull(startRowNumber) ? (parseInt(startRowNumber) - 1) : null)
        .setEndColumnIndex(
            nonNull(endColumnName) ? getColumnNumberFromA1Notation(endColumnName) + 1 : null)
        .setEndRowIndex(nonNull(endRowNumber) ? parseInt(endRowNumber) : null);
  }

  /**
   * Validate if there at least one element in the list of list array
   *
   * @param data the Elements to be written to the spreadsheet
   * @return true if list contains at least one element false otherwise
   */
  public static boolean isValidData(List<List<Object>> data) {
    return nonNull(data) && !data.isEmpty() && !data.get(0).isEmpty();
  }
}

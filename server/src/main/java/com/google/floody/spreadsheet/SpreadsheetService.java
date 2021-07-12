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

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Methods to abstract Spreadsheet operations for reading and writing data to spreadsheets */
public interface SpreadsheetService {

  String getSpreadsheetId();

  /** Returns the URI of the spreadsheet. */
  @Nullable
  String getSpreadsheetUri();

  /**
   * Sets the default dimension an operation should apply
   *
   * @param majorDimension the default sequence for cells retrieval
   * @return instance of self to enable call chaining
   */
  SpreadsheetService setMajorDimension(MajorDimension majorDimension);

  /**
   * Sets how input data should be interpreted.
   *
   * @return instance of self to enable call chaining
   */
  SpreadsheetService setValueRenderOption(ValueRenderOption renderOption);

  /**
   * Sets how values should be rendered in the output
   *
   * @return instance of self to enable call chaining
   */
  SpreadsheetService setValueInputOption(ValueInputOption valueInputOption);

  /**
   * Creates a new sheet in the spreadsheet with given name/title and headers
   *
   * @param sheetName the name of the sheet to be set
   * @param headers the values to be put in the first row of the spreadsheet
   * @return the id of the newly created sheet
   * @throws IOException when there is error in creating a new sheet
   */
  int createNewSheet(String sheetName, @Nullable List<String> headers) throws IOException;

  /**
   * Deletes the sheet in the initialized spreadsheet.
   *
   * @param sheetId of the sheet to be deleted
   */
  void deleteSheet(Integer sheetId);

  /**
   * Deletes the sheet in the initialized spreadsheet.
   *
   * @param sheetName the title of the sheet to delete
   */
  void deleteSheet(String sheetName);

  /**
   * Erases everything in the given range of the sheet
   *
   * @param sheetName of the sheet to be erased
   * @param rangeA1Notation the range of cells to be cleared in A1Notation
   * @throws IOException when there is an error in clearing data
   */
  void clearData(String sheetName, String rangeA1Notation) throws IOException;

  /**
   * Protects/Locks the given range (defined by {@code startColumn} and {@code endColumn}) to ensure
   * only editors defined by Google Groups {@code groupEditors} are able to make changes
   *
   * @param sheetId Sheet Id of the Sheet to protect
   * @param a1NotationRange the range in an A1 notation Format*
   * @param warningText the message to display when user tries to edit the protected range
   * @param groupEditors set of GoogleGroup Ids to be editors
   * @throws IOException when there is an error in locking the ranges
   */
  void protectRange(
      Integer sheetId,
      String a1NotationRange,
      @Nullable String warningText,
      @Nullable List<String> groupEditors)
      throws IOException;

  /**
   * Sets up data-validation in the given column to ensure users' are allowed to enter one of the
   * pre-specified values
   *
   * @param sheetId of the sheet to setup validation
   * @param column the column number (starts with 0 = Col.A)
   * @param values array of specific valid values for the column
   * @param helpText the message to show when uses' try to enter non-compliant values
   * @throws IOException when there is an error in writing validation rules
   */
  void setDataValidationForValuesInList(
      Integer sheetId, Integer column, List<String> values, String helpText) throws IOException;

  /**
   * Read the given sheet Range and output data by transforming rows into objects through the
   * provided transformer method
   *
   * @param sheetName name of the sheet to read from
   * @param range the a1 notation of the data range to read from
   * @param transformer implementation of methods to transform a sheet Row/column to an object of
   *     the given type
   * @param <T> the type of Object
   * @return list of Objects transformed from the spreadsheet
   * @throws IOException when there is error in reading from the spreadsheet
   */
  <T> List<T> retrieveData(String sheetName, String range, SpreadsheetRowTransformer<T> transformer)
      throws IOException;

  /**
   * Writes the data objects to a spreadsheet as rows/columns based on set {@link MajorDimension} It
   * automatically chooses the Range from 2nd Row/Column onwards
   *
   * @param sheetName the name of the sheet to write to
   * @param transformer implementation of methods to transform the given Object type to a sheet
   *     row/column
   * @param data the list of objects to write to the spreadsheet
   * @param <T> the type of objects to store in spreadsheet
   * @return the number of rows/columns updated
   * @throws IOException when there is an error in writing values
   */
  <T> int storeDataWithTransform(
      String sheetName, SpreadsheetRowTransformer<T> transformer, Collection<T> data)
      throws IOException;

  /**
   * Write the list of objects into a spreadsheet started from the {@code startCell}. You may
   * include the column's header as the first row of data.
   *
   * @param sheetName the name of the sheet to write to
   * @param startCell the start cell of the spreadsheet (i.e. A2)
   * @param data the list of objects to write to the spreadsheet.
   * @return the number of rows/columns updated
   * @throws IOException when there is an error in writing values
   */
  int storeData(String sheetName, String startCell, List<List<Object>> data) throws IOException;

  /**
   * Adds new rows at the bottom of the spreadsheet.
   *
   * @param sheetName the name of the sheet to add rows to.
   * @param numRows the number of rows to add.
   * @throws IOException in case of an error operation.
   */
  void addMoreRows(String sheetName, int numRows) throws IOException;

  String getSpreadsheetTitle() throws IOException;

  /**
   * Sets the spreadsheet's title as the given value.
   *
   * @throws IOException in case of an API error.
   */
  void setSpreadsheetTitle(String newTitle) throws IOException;

  ImmutableMap<Integer, Integer> lockSheets(String... sheetNames) throws IOException;

  /**
   * Removes the protected range from the spreadsheet.
   *
   * @param protectedRangeIds one or more protected Range Ids.
   */
  void unlockSheet(Integer... protectedRangeIds) throws IOException;

  void unlockSheets(Collection<Integer> protectedRangeIds) throws IOException;

  void unlockSheets(String... sheetNames) throws IOException;

  SpreadsheetMetaReader metaReader() throws IOException;

  SpreadsheetMetaWriter metaWriter() throws IOException;
}

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

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.util.MimeType;

/**
 * Protobuf and Jackson based composite message converter to allow responding with both Proto and
 * POJO objects.
 */
public class CompositeMessageConverter implements HttpMessageConverter<Object> {

  private static final MimeType JSON_MIME_TYPE = MimeType.valueOf("application/json");

  private final ProtobufJsonFormatHttpMessageConverter protoConverter =
      new ProtobufJsonFormatHttpMessageConverter();
  private final MappingJackson2HttpMessageConverter jacksonConverter =
      new MappingJackson2HttpMessageConverter();
  private final StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return true;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return true;
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return Stream.of(protoConverter, jacksonConverter, stringConverter)
        .map(HttpMessageConverter::getSupportedMediaTypes)
        .flatMap(List::stream)
        .distinct()
        .collect(toImmutableList());
  }

  @Override
  public Object read(Class clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {

    if (Message.class.isAssignableFrom(clazz)) {
      return protoConverter.read(clazz, inputMessage);
    }

    if (String.class.isAssignableFrom(clazz)) {
      return stringConverter.read(clazz, inputMessage);
    }

    return jacksonConverter.read(clazz, inputMessage);
  }

  @Override
  public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {

    if (o instanceof Message) {

      if (contentType.equals(JSON_MIME_TYPE)) {
        protoConverter.write((Message) o, contentType, outputMessage);
      } else {
        var protoText = TextFormat.printer().printToString((Message) o);
        stringConverter.write(protoText, contentType, outputMessage);
      }

      return;
    }

    if (o instanceof String) {
      stringConverter.write((String) o, contentType, outputMessage);
      return;
    }

    jacksonConverter.write(o, contentType, outputMessage);
  }
}

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

package com.google.floody.filter;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.transforms.CompositeMessageConverter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CompositeConverterConfiguration implements WebMvcConfigurer {

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new CompositeMessageConverter());
    GoogleLogger.forEnclosingClass().atInfo().log("Updated converters:%n%s", converters);
  }

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    return new Jackson2ObjectMapperBuilder()
        .modules(new JodaModule())
        .postConfigurer(
            objectMapper ->
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false));
  }
}

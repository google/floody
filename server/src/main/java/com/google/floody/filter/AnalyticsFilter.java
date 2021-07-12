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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.floody.model.FloodyProperties;
import com.google.floody.service.GoogleAnalyticsTracker;
import com.google.floody.transforms.AnalyticsHitDataExtractor;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AnalyticsFilter extends OncePerRequestFilter {

  @Autowired private FloodyProperties floodyProperties;

  @Override
  public void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    if (isAnalyticsEnabledUri(request.getRequestURI())) {
      AnalyticsHitDataExtractor hitDataExtractor =
          AnalyticsHitDataExtractor.builder()
              .setGaPropertyId(floodyProperties.getAnalyticsPropertyId())
              .setRequest(request)
              .build();

      new GoogleAnalyticsTracker(
              floodyProperties.getAnalyticsPropertyId(),
              floodyProperties.getLogsPiiAttributes(),
              isDebugEnabled(request))
          .postToAnalyticsServer(hitDataExtractor.extract());
    }

    filterChain.doFilter(request, response);
  }

  private boolean isAnalyticsEnabledUri(String uri) {
    return Optional.ofNullable(floodyProperties.getAnalyticsExcludedUris())
        .map(excludedUris -> excludedUris.stream().noneMatch(uri::startsWith))
        .orElse(Boolean.TRUE);
  }

  private boolean isDebugEnabled(HttpServletRequest request) {
    var debugParam = request.getParameter(floodyProperties.getLogPiiQueryParam());
    return isNotBlank(debugParam)
        && (debugParam.equals("1") || debugParam.equalsIgnoreCase("true"));
  }

  @Bean
  public FilterRegistrationBean<AnalyticsFilter> analyticsFilterBean() {

    var registrationBean = new FilterRegistrationBean<AnalyticsFilter>();

    registrationBean.setFilter(this);
    registrationBean.addUrlPatterns("/admin/*", "/floody/*", "/gtmrequest/*", "/user/*");

    return registrationBean;
  }
}

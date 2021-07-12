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

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.auth.http.AuthHttpConstants;
import com.google.floody.auth.AccessTokenCredentialService;
import com.google.floody.model.FloodyProperties;
import com.google.floody.service.ServicesFactory;
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
public final class UserServicesFactoryInjectionFilter extends OncePerRequestFilter {

  @Autowired private FloodyProperties floodyProperties;

  /** Adds a services Factory bean if user credentials are present. */
  @Override
  public void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    extractBearerToken(request)
        .ifPresent(
            accessToken ->
                request.setAttribute(
                    "userServicesFactory",
                    new ServicesFactory(
                        new AccessTokenCredentialService(accessToken), floodyProperties)));

    filterChain.doFilter(request, response);
  }

  private static Optional<String> extractBearerToken(HttpServletRequest request) {
    var bearerAuth = request.getHeader(AuthHttpConstants.AUTHORIZATION);

    if (!isBlank(bearerAuth) && bearerAuth.startsWith(AuthHttpConstants.BEARER)) {
      return Optional.of(bearerAuth.split(" ")[1]);
    }

    return Optional.empty();
  }

  @Bean
  public FilterRegistrationBean<UserServicesFactoryInjectionFilter>
      userServicesFactoryInjectionFilterBean() {

    var registrationBean = new FilterRegistrationBean<UserServicesFactoryInjectionFilter>();

    registrationBean.setFilter(this);
    registrationBean.addUrlPatterns(
        "/admin/*", "/floody/*", "/gtmrequest/*", "/user/*", "/crontasks/*");

    return registrationBean;
  }
}

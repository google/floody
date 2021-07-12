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

import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.flogger.GoogleLogger;
import com.google.floody.service.DatastoreService;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectifyConfiguration {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Bean
  public FilterRegistrationBean<ObjectifyFilter> objectifyFilterRegistration() {
    final FilterRegistrationBean<ObjectifyFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new ObjectifyFilter());
    registration.addUrlPatterns("/gtmrequest/*");
    registration.setOrder(1);
    return registration;
  }

  @Bean
  public ServletListenerRegistrationBean<ObjectifyListener> listenerRegistrationBean() {
    ServletListenerRegistrationBean<ObjectifyListener> bean =
        new ServletListenerRegistrationBean<>();
    bean.setListener(new ObjectifyListener());
    return bean;
  }

  @WebListener
  public static class ObjectifyListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      var objectifyFactory = new ObjectifyFactory(buildDatastoreOptions().getService());
      JodaTimeTranslators.add(objectifyFactory);

      ObjectifyService.init(objectifyFactory);
      DatastoreService.registerEntities();
    }

    private DatastoreOptions buildDatastoreOptions() {
      var executionEnvironment = System.getenv("EXECUTION_ENVIRONMENT");

      logger.atFine().log("Environment: %s", executionEnvironment);
      if ("local".equals(executionEnvironment)) {
        var emulatorHost = System.getenv("DATASTORE_EMULATOR_HOST");
        logger.atInfo().log("using local Datastore emulator: " + emulatorHost);

        return DatastoreOptions.newBuilder().setHost("http://" + emulatorHost).build();
      }

      return DatastoreOptions.getDefaultInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
  }
}

#
# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
JUNIT_VERSION = "4.13"

AUTOVALUE_VERSION = "1.8.1"

FLOGGER_VERSION = "0.6"

JACKSON_VERSION = "2.12.2"

GUAVA_VERSION = "30.1.1-jre"

#SPRING
SPRING_BOOT_VERSION = "2.5.1"

SPRING_FRAMEWORK_VERSION = "5.3.8"

CORE_MAVEN_REPOSITORIES = [
    "https://repo1.maven.org/maven2",
]

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_java",
    sha256 = "34b41ec683e67253043ab1a3d1e8b7c61e4e8edefbcad485381328c934d072fe",
    url = "https://github.com/bazelbuild/rules_java/releases/download/4.0.0/rules_java-4.0.0.tar.gz",
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

http_archive(
    name = "rules_jvm_external",
    sha256 = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169",
    strip_prefix = "rules_jvm_external-4.0",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/4.0.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    name = "maven",
    artifacts = [
        "com.google.auth:google-auth-library-oauth2-http:0.26.0",
        "com.google.auth:google-auth-library-credentials:0.26.0",
        "com.google.apis:google-api-services-oauth2:v2-rev157-1.25.0",
        "com.google.apis:google-api-services-sheets:v4-rev612-1.25.0",
        "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
        "com.google.apis:google-api-services-tagmanager:v1-rev20200826-1.31.0",
        "com.google.apis:google-api-services-dfareporting:v3.5-rev20210524-1.31.5",
        "com.google.cloud:google-cloud-datastore:1.107.0",
        "org.checkerframework:checker-qual:3.10.0",
        "joda-time:joda-time:2.10.10",
        "org.joda:joda-convert:2.2.1",
        "org.joda:joda-money:1.0.1",
        "com.google.guava:guava:%s" % GUAVA_VERSION,
        "org.apache.commons:commons-lang3:3.12.0",
        "com.google.auto.value:auto-value-annotations:%s" % AUTOVALUE_VERSION,
        "com.google.auto.value:auto-value:%s" % AUTOVALUE_VERSION,
        "com.googlecode.objectify:objectify:6.0.7",
        "org.projectlombok:lombok:1.18.18",
        "com.google.flogger:flogger:%s" % FLOGGER_VERSION,
        "com.google.flogger:google-extensions:%s" % FLOGGER_VERSION,
        "com.google.flogger:flogger-system-backend:%s" % FLOGGER_VERSION,
        "com.google.http-client:google-http-client-gson:1.38.1",
        "com.google.http-client:google-http-client:1.38.1",
        "com.fasterxml.jackson.core:jackson-databind:%s" % JACKSON_VERSION,
        "com.fasterxml.jackson.core:jackson-annotations:%s" % JACKSON_VERSION,
        "com.fasterxml.jackson.core:jackson-core:%s" % JACKSON_VERSION,
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:%s" % JACKSON_VERSION,
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.servlet:javax.servlet-api:4.0.1",
        "org.hamcrest:hamcrest-library:1.3",
        "junit:junit:%s" % JUNIT_VERSION,
        "com.squareup.okhttp3:okhttp:4.9.0",
        "com.squareup.okio:okio:2.8.0",
        "org.springframework.boot:spring-boot-starter-web:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter:%s" % SPRING_BOOT_VERSION,
        "jakarta.annotation:jakarta.annotation-api:1.3.5",
        "org.springframework.boot:spring-boot-starter-json:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-autoconfigure:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-logging:%s" % SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-loader:%s" % SPRING_BOOT_VERSION,
        "org.yaml:snakeyaml:1.27",
        "org.springframework.boot:spring-boot-starter-jetty:%s" % SPRING_BOOT_VERSION,
        "org.springframework:spring-beans:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-core:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-jcl:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-web:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-webmvc:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-aop:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-context:%s" % SPRING_FRAMEWORK_VERSION,
        "org.springframework:spring-expression:%s" % SPRING_FRAMEWORK_VERSION,
    ],
    excluded_artifacts = [
        "org.springframework.boot:spring-boot-starter-tomcat",
    ],
    fetch_javadoc = True,
    fetch_sources = True,
    repositories = CORE_MAVEN_REPOSITORIES,
)

# Proto Rules
http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-97d8af4dc474595af3900dd85cb3a29ad28cc313",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz",
    ],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

http_archive(
    name = "io_bazel_rules_closure",
    sha256 = "d66deed38a0bb20581c15664f0ab62270af5940786855c7adc3087b27168b529",
    strip_prefix = "rules_closure-0.11.0",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_closure/archive/0.11.0.tar.gz",
        "https://github.com/bazelbuild/rules_closure/archive/0.11.0.tar.gz",
    ],
)

load("@io_bazel_rules_closure//closure:repositories.bzl", "rules_closure_dependencies", "rules_closure_toolchains")

rules_closure_dependencies()

rules_closure_toolchains()

http_archive(
    name = "rules_python",
    sha256 = "778197e26c5fbeb07ac2a2c5ae405b30f6cb7ad1f5510ea6fdac03bded96cc6f",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_python/releases/download/0.2.0/rules_python-0.2.0.tar.gz",
        "https://github.com/bazelbuild/rules_python/releases/download/0.2.0/rules_python-0.2.0.tar.gz",
    ],
)

http_archive(
    name = "rules_spring",
    sha256 = "4afceddd222bfd596f09591fd41f0800e57dd2d49e3fa0bda67f1b43149e8f3e",
    urls = [
        "https://github.com/salesforce/rules_spring/releases/download/2.1.3/rules-spring-2.1.3.zip",
    ],
)

load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "floody",
    srcs = [
        "FloodyApplication.java",
    ],
    resources = [
        "//server/src/main/resources",
    ],
    visibility = ["//:floody"],
    runtime_deps = [
        "//libs/springboot:starter-web",
    ],
    deps = [
        "//libs/commons:guava",
        "//libs/flogger",
        "//libs/flogger:flogger-backend",
        "//libs/jackson2:jackson2-databind",
        "//libs/okhttp:okhttp-client",
        "//server/src/main/java/com/google/floody/auth",
        "//server/src/main/java/com/google/floody/controller",
        "//server/src/main/java/com/google/floody/exceptions",
        "//server/src/main/java/com/google/floody/filter",
        "//server/src/main/java/com/google/floody/model",
        "//server/src/main/java/com/google/floody/service",
        "//server/src/main/java/com/google/floody/spreadsheet",
        "//server/src/main/java/com/google/floody/time",
        "//server/src/main/java/com/google/floody/transforms",
        "@maven//:com_google_http_client_google_http_client",
        "@maven//:com_google_http_client_google_http_client_gson",
        "@maven//:org_springframework_boot_spring_boot",
        "@maven//:org_springframework_boot_spring_boot_autoconfigure",
    ],
)

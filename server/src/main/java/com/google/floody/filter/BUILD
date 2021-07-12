load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "filter",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = [
        "//libs/commons:apache-lang3",
        "//libs/commons:guava",
        "//libs/flogger",
        "//libs/jackson2:jackson2-databind",
        "//libs/jackson2:jackson2-joda",
        "//libs/objectify",
        "//server/src/main/java/com/google/floody/auth",
        "//server/src/main/java/com/google/floody/model",
        "//server/src/main/java/com/google/floody/service",
        "//server/src/main/java/com/google/floody/transforms",
        "@maven//:com_google_auth_google_auth_library_credentials",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:com_google_cloud_google_cloud_datastore",
        "@maven//:org_springframework_boot_spring_boot",
        "@maven//:org_springframework_spring_beans",
        "@maven//:org_springframework_spring_context",
        "@maven//:org_springframework_spring_web",
        "@maven//:org_springframework_spring_webmvc",
    ],
)

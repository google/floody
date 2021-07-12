load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "controller",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = [
        "//libs/commons:apache-lang3",
        "//libs/commons:guava",
        "//libs/flogger",
        "//libs/joda:time",
        "//libs/objectify",
        "//protos:protos_java",
        "//server/src/main/java/com/google/floody/exceptions",
        "//server/src/main/java/com/google/floody/model",
        "//server/src/main/java/com/google/floody/service",
        "//server/src/main/java/com/google/floody/spreadsheet",
        "@maven//:com_google_apis_google_api_services_oauth2",
        "@maven//:com_google_auth_google_auth_library_credentials",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:com_google_cloud_google_cloud_datastore",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_protobuf_protobuf_java_util",
        "@maven//:org_springframework_spring_beans",
        "@maven//:org_springframework_spring_context",
        "@maven//:org_springframework_spring_web",
    ],
)

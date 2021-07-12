load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "model",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = [
        "//libs/autovalue",
        "//libs/commons:apache-lang3",
        "//libs/commons:guava",
        "//libs/flogger",
        "//libs/jackson2:jackson2-databind",
        "//libs/joda:time",
        "//libs/objectify",
        "//protos:protos_java",
        "//server/src/main/java/com/google/floody/exceptions",
        "@maven//:com_google_apis_google_api_services_dfareporting",
        "@maven//:com_google_apis_google_api_services_drive",
        "@maven//:com_google_auto_value_auto_value_annotations",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_protobuf_protobuf_java_util",
        "@maven//:org_checkerframework_checker_qual",
        "@maven//:org_springframework_boot_spring_boot",
    ],
)

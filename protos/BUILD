package(default_visibility = ["//:floody"])

load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "analytics_proto",
    srcs = ["analytics.proto"],
)

proto_library(
    name = "sheet_objects_proto",
    srcs = ["sheet_objects.proto"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

proto_library(
    name = "file_operations_proto",
    srcs = ["file_operations.proto"],
    deps = [":sheet_objects_proto"],
)

proto_library(
    name = "user_operations_proto",
    srcs = ["user_operations.proto"],
    deps = [
        ":sheet_objects_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

proto_library(
    name = "gtm_operations_proto",
    srcs = ["gtm_operations.proto"],
)

proto_library(
    name = "admin_operations_proto",
    srcs = ["admin_operations.proto"],
)

java_proto_library(
    name = "protos_java",
    deps = [
        ":admin_operations_proto",
        ":analytics_proto",
        ":file_operations_proto",
        ":gtm_operations_proto",
        ":sheet_objects_proto",
        ":user_operations_proto",
    ],
)

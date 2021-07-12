load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "flogger",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

java_library(
    name = "flogger-backend",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_google_flogger_flogger_system_backend",
    ],
)

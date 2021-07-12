load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "spreadsheet",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = [
        "//libs/commons:apache-lang3",
        "//libs/commons:guava",
        "//libs/flogger",
        "//libs/jackson2:jackson2-databind",
        "//libs/joda:time",
        "//server/src/main/java/com/google/floody/auth",
        "//server/src/main/java/com/google/floody/model",
        "@maven//:com_google_api_client_google_api_client",
        "@maven//:com_google_apis_google_api_services_dfareporting",
        "@maven//:com_google_apis_google_api_services_drive",
        "@maven//:com_google_apis_google_api_services_sheets",
        "@maven//:com_google_apis_google_api_services_tagmanager",
        "@maven//:com_google_auth_google_auth_library_credentials",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:com_google_auto_value_auto_value_annotations",
        "@maven//:com_google_http_client_google_http_client",
        "@maven//:com_google_http_client_google_http_client_gson",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

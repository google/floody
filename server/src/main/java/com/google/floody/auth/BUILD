load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "auth",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = [
        "//libs/commons:apache-lang3",
        "//libs/commons:guava",
        "@maven//:com_google_auth_google_auth_library_credentials",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

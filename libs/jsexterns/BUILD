package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_binary", "closure_js_library")

closure_js_library(
    name = "gapi",
    srcs = ["gapi.js"],
    no_closure_library = True,
)

closure_js_library(
    name = "gapi.drive",
    srcs = ["gapi.drive.js"],
    no_closure_library = True,
    deps = [":gapi"],
)

closure_js_library(
    name = "gapi.auth2",
    srcs = ["gapi.auth2.js"],
    no_closure_library = True,
    deps = [":gapi"],
)

closure_js_library(
    name = "google_client_externs",
    no_closure_library = True,
    exports = [
        ":gapi",
        ":gapi.auth2",
        ":gapi.drive",
    ],
)

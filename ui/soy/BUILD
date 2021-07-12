load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_template_library")

# Soy Library For UI
closure_js_template_library(
    name = "manage_soy_js",
    srcs = [
        "gtm.soy",
        "manage.soy",
        "templates.soy",
    ],
    visibility = ["//:floody"],
)

closure_js_template_library(
    name = "file_selector_soy_js",
    srcs = ["file_selector.soy"],
    visibility = ["//:floody"],
)

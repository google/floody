load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_binary", "closure_js_library")

COMMON_JS_DEPS = [
    "//ui/soy:manage_soy_js",
    "@io_bazel_rules_closure//closure/library:goog",
    "@io_bazel_rules_closure//closure/library/dom:safe",
    "@io_bazel_rules_closure//closure/library/soy",
]

# Builds floody_ui.js
closure_js_library(
    name = "common_ui_lib",
    srcs = ["common_ui.js"],
    lenient = True,
    deps = COMMON_JS_DEPS + ["//libs/jsexterns:google_client_externs"],
)

closure_js_library(
    name = "floody_ui_lib",
    srcs = [
        "floody_ui.js",
    ],
    lenient = True,
    deps = COMMON_JS_DEPS + [
        ":common_ui_lib",
        "//ui/soy:file_selector_soy_js",
    ],
)

closure_js_binary(
    name = "floody_ui_bin",
    compilation_level = "SIMPLE",
    entry_points = [
        "common.logic",
        "floodyUi.logic",
    ],
    visibility = ["//:floody"],
    deps = [":floody_ui_lib"],
)

# Manage Binary
closure_js_library(
    name = "manage_ui_lib",
    srcs = [
        "manage_ui.js",
    ],
    lenient = True,
    deps = COMMON_JS_DEPS + [":common_ui_lib"],
)

closure_js_binary(
    name = "manage_ui_bin",
    compilation_level = "SIMPLE",
    entry_points = [
        "common.logic",
        "manageUi.logic",
    ],
    visibility = ["//:floody"],
    deps = [":manage_ui_lib"],
)

# GTM Binary
closure_js_library(
    name = "gtm_ui_lib",
    srcs = [
        "gtm_ui.js",
    ],
    lenient = True,
    deps = COMMON_JS_DEPS + [":common_ui_lib"],
)

closure_js_binary(
    name = "gtm_ui_bin",
    compilation_level = "SIMPLE",
    entry_points = [
        "common.logic",
        "gtmUi.logic",
    ],
    visibility = ["//:floody"],
    deps = [":gtm_ui_lib"],
)

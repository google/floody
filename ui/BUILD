filegroup(
    name = "html_files",
    srcs = [
        "gtm.html",
        "index.html",
        "manage.html",
        "robots.txt",
        "security_and_privacy.html",
    ],
)

filegroup(
    name = "ui_files",
    srcs = [
        "favicon.ico",
        ":html_files",
        "//ui/images",
        "//ui/scripts:floody_ui_bin.js",
        "//ui/scripts:gtm_ui_bin.js",
        "//ui/scripts:manage_ui_bin.js",
        "//ui/style",
    ],
    visibility = ["//:floody"],
)

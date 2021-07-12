load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jackson2-databind",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
    ],
)

java_library(
    name = "jackson2-joda",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_joda",
    ],
)

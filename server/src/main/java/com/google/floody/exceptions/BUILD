load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "exceptions",
    srcs = glob(["*.java"]),
    visibility = ["//:floody"],
    deps = ["@maven//:org_springframework_spring_web"],
)

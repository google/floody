load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@rules_spring//springboot:springboot.bzl", "springboot")

springboot(
    name = "floodyapp",
    boot_app_class = "com.google.floody.FloodyApplication",
    java_library = "//server/src/main/java/com/google/floody",
)

java_binary(
    name = "floodyapp-local",
    main_class = "com.google.floody.FloodyApplication",
    runtime_deps = [
        "//server/src/main/java/com/google/floody",
    ],
)

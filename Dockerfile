FROM gcr.io/cloud-marketplace-containers/google/bazel
COPY . /floodysrc
WORKDIR /floodysrc
RUN wget -q "https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-amd64" && \
    chmod +x bazelisk-linux-amd64
RUN ./bazelisk-linux-amd64 build //server:floodyapp

# Define the Cloud Run Container Image
FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=0 /floodysrc/bazel-bin/server/floodyapp.jar .
# Run the web service on container startup.
CMD ["java", "-jar", "floodyapp.jar"]

FROM clojure:openjdk-8-lein-stretch

ARG GRAAL_VERSION=19.2.0.1
ENV ARTIFACT_VERSION=0.1.0-SNAPSHOT
ENV LANG=en_US.UTF-8

ENV GRAALVM_PKG=https://github.com/oracle/graal/releases/download/vm-$GRAAL_VERSION/graalvm-ce-linux-amd64-$GRAAL_VERSION.tar.gz \
    JAVA_HOME=/usr/local/graalvm-ce-$GRAAL_VERSION/ \
    GRAALVM_HOME=/usr/local/graalvm-ce-$GRAAL_VERSION/ \
    PATH=/usr/local/graalvm-ce-$GRAAL_VERSION/bin/:$PATH

RUN set -eux && \
    apt-get -y update && \
    apt-get -y install build-essential zlib1g-dev && \
    wget -q $GRAALVM_PKG && \
    tar -xzf graalvm-ce-linux-amd64-$GRAAL_VERSION.tar.gz && \
    mv graalvm-ce-$GRAAL_VERSION $JAVA_HOME && \
    gu install native-image

WORKDIR /workspace
COPY . /workspace

RUN lein native-image

LABEL repository="https://github.com/hden/run-env"
LABEL maintainer="Haokang Den <haokang.den@gmail.com>"

FROM gcr.io/cloud-builders/gcloud-slim@sha256:2d44a5dccadc63ec56338f64f7bf9b0ce42361dfbbe014c387a77a9a0a2cc3f1
ENV ARTIFACT_VERSION=0.1.0-SNAPSHOT

RUN set -eux && \
    apt-get -y update && \
    apt-get -y install jq && \
    yes | gcloud components update && \
    yes | gcloud components install beta

COPY --from=0 /workspace/target/default+uberjar/run-env-$ARTIFACT_VERSION /usr/local/bin/run-env
COPY docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

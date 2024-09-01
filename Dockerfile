FROM clojure:openjdk-11-lein

ARG GRAAL_VERSION=22.3.0
ENV ARTIFACT_VERSION=0.1.0-SNAPSHOT
ENV LANG=en_US.UTF-8

ENV GRAALVM_PKG=https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$GRAAL_VERSION/graalvm-ce-java11-linux-amd64-$GRAAL_VERSION.tar.gz \
    JAVA_HOME=/usr/local/graalvm-ce-java11-$GRAAL_VERSION/ \
    GRAALVM_HOME=/usr/local/graalvm-ce-java11-$GRAAL_VERSION/ \
    PATH=/usr/local/graalvm-ce-java11-$GRAAL_VERSION/bin/:$PATH

RUN set -eux && \
    apt-get -y update && \
    apt-get -y install build-essential wget zlib1g-dev && \
    wget -q $GRAALVM_PKG && \
    tar -xzf graalvm-ce-java11-linux-amd64-$GRAAL_VERSION.tar.gz && \
    mv graalvm-ce-java11-$GRAAL_VERSION $JAVA_HOME && \
    gu install native-image

WORKDIR /workspace
COPY . /workspace

RUN lein native-image

LABEL repository="https://github.com/hden/run-env"
LABEL maintainer="Haokang Den <haokang.den@gmail.com>"

FROM gcr.io/cloud-builders/gcloud-slim@sha256:119574563bb165ad6d5dcfae5b51dfe6102f308bd01af0e6dbd5f7425d18d738
ENV ARTIFACT_VERSION=0.1.0-SNAPSHOT

RUN set -eux && \
    apt-get -y update && \
    apt-get -y install jq && \
    yes | gcloud components update && \
    yes | gcloud components install beta

COPY --from=0 /workspace/target/default+uberjar/run-env-$ARTIFACT_VERSION /usr/local/bin/run-env
COPY docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

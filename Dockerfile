FROM ubuntu:bionic-20200403 as builder

WORKDIR /root

COPY . /root/runelite

ARG DEBIAN_FRONTEND=noninteractive
ARG LANG=en_US.UTF-8
ARG LANGUAGE=en_US:en
ARG LC_ALL=en_US.UTF-8

RUN apt-get update && apt-get install -y software-properties-common wget && \
    wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add - && \
    add-apt-repository -y https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ && add-apt-repository -y ppa:apt-fast/stable && \
    apt-get update && apt-get install -y apt-fast && \
    apt-fast install -y adoptopenjdk-14-openj9 maven locales ttf-mscorefonts-installer fontconfig && \
    sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen && \
    fc-cache -f -v && \
    mvn install -f /root/runelite/pom.xml && mkdir -p /root/runelite-client && mv /root/runelite/runelite-client/target/client-*-SNAPSHOT-shaded.jar /root/runelite-client/RuneLite.jar


FROM scratch
COPY --from=builder /root/runelite-client/RuneLite.jar .

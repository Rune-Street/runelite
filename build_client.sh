#!/bin/bash
cd "${0%/*}"

mvn install -DskipTests -U -f runelite-client/pom.xml

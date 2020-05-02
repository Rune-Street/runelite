#!/bin/bash
cd "${0%/*}"

mvn clean install -DskipTests -U

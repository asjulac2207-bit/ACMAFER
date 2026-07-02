#!/bin/sh
# Maven Wrapper Script
set -e
MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"
DOWNLOAD_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2)
MVN_BIN=""
if command -v mvn > /dev/null 2>&1; then
  MVN_BIN="mvn"
fi
if [ -n "$MVN_BIN" ]; then
  exec "$MVN_BIN" "$@"
fi
echo "Error: Maven not found. Please install Maven 3.8+ from https://maven.apache.org"
exit 1

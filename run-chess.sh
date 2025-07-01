#!/bin/bash
# Chess Game Launcher Script with FlatLaf Support

cd "$(dirname "$0")"

# Build the project if needed
echo "Building Chess Game..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi

# Get the dependency classpath
CLASSPATH="target/classes"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/ch/qos/logback/logback-classic/1.2.11/logback-classic-1.2.11.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/ch/qos/logback/logback-core/1.2.11/logback-core-1.2.11.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/formdev/flatlaf/3.2.5/flatlaf-3.2.5.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/formdev/flatlaf-extras/3.2.5/flatlaf-extras-3.2.5.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/github/weisj/jsvg/1.2.0/jsvg-1.2.0.jar"

echo "Starting Chess Game with FlatLaf..."
java -cp "$CLASSPATH" com.chess.Main "$@"

function copyEnvVarsToGradleProperties {
	mkdir -p "$HOME/.gradle"
    GRADLE_PROPERTIES=$HOME"/.gradle/gradle.properties"
    export GRADLE_PROPERTIES
    echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

    if [ ! -f "$GRADLE_PROPERTIES" ]; then
        echo "Gradle Properties does not exist"

        echo "Creating Gradle Properties file..."
        touch $GRADLE_PROPERTIES

        echo "Writing TEST_API_KEY to gradle.properties..."
        echo "rezolveMavenPassword=$MAVEN_PASSWORD" >> $GRADLE_PROPERTIES
    fi
}
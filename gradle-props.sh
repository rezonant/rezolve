function copyEnvVarsToGradleProperties {
	mkdir -p "$HOME/.gradle"
    GRADLE_PROPERTIES="$HOME/.gradle/gradle.properties"
    export GRADLE_PROPERTIES
    echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

    if [ ! -f "$GRADLE_PROPERTIES" ]; then
        touch $GRADLE_PROPERTIES
    fi

    echo "Writing MAVEN_PASSWORD to gradle.properties..."
    echo "rezolveMavenPassword=$MAVEN_PASSWORD" >> $GRADLE_PROPERTIES
}
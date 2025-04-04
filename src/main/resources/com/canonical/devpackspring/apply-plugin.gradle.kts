allprojects {
    afterEvaluate {
        if (name == rootProject.name) {
            if (!plugins.hasPlugin("%s")) { // plugin id
                apply<%s>() // plugin class name
                plugins.withId("%s") {
                    %s
                }
            }
        }
    }
}

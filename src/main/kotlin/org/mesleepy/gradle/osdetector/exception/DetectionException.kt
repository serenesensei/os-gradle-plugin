package org.mesleepy.gradle.osdetector.exception

class DetectionException(message: String?) : RuntimeException(message) {

    companion object {
        private const val serialVersionUID = 7787197994442254320L
    }
}

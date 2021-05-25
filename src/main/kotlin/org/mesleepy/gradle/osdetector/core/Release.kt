package org.mesleepy.gradle.osdetector.core

/**
 * Accessor to information about the current OS release.
 */
class Release(private val impl: DetectorImpl) {

    /**
     * Returns the release ID.
     */
    val id: String?
        get() = impl.detectedProperties[Detector.DETECTED_RELEASE] as String?

    /**
     * Returns the version ID.
     */
    val version: String?
        get() = impl.detectedProperties[Detector.DETECTED_RELEASE_VERSION] as String?

    /**
     * Returns `true` if this release is a variant of the given base release (for example,
     * ubuntu is "like" debian).
     */
    fun isLike(baseRelease: String): Boolean = impl
        .detectedProperties
        .containsKey(Detector.DETECTED_RELEASE_LIKE_PREFIX + baseRelease)
}

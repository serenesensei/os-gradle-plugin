/*
 * Copyright 2021 me-sleepy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

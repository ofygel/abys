package com.example.abys

/**
 * Minimal BuildConfig replacement used in tests.
 * The Gradle plugin normally generates this, but the
 * lightweight environment we run in does not. We only
 * need the DEBUG flag for conditional logging.
 */
object BuildConfig {
    const val DEBUG: Boolean = true
}


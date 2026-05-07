package com.hadi.striff;

/**
 * Versioned serialization contract for cached diff payloads shared across
 * services. Bump this only when the serialized CodeDiff shape becomes
 * incompatible with previous readers.
 */
public final class StriffLibVersion {

    public static final int CODE_DIFF_FORMAT_VERSION = 1;

    private StriffLibVersion() {
    }
}

package com.atlassian.pocketknife.api.version;

/**
 * Use this class to create SoftwareVersion objects.
 */
public class VersionKit {
    /**
     * Parses and returns a SoftwareVersion object representing the dotted number string.
     *
     * @param versionString the input version
     * @return a version domain object
     * @throws IllegalArgumentException if the string is not N.N.N
     */
    public static SoftwareVersion parse(final String versionString) {
        return new SoftwareVersion(versionString);
    }

    public static SoftwareVersion version(final int majorVersion, final int... versions) {
        if (versions.length <= 2) {
            int minorVersion = readArray(versions, 0, 0);
            int bugFixVersion = readArray(versions, 1, 0);
            return new SoftwareVersion(majorVersion, minorVersion, bugFixVersion);
        } else {
            throw new IllegalArgumentException("Too many version parameters supplied");
        }
    }

    private static int readArray(int[] versions, int index, int defaultVal) {
        if (index >= versions.length) {
            return defaultVal;
        }
        return versions[index];
    }
}

package com.atlassian.pocketknife.api.version;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates information about a software version and allows common comparisons.
 */
public class SoftwareVersion implements Comparable<SoftwareVersion>
{
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.?(-.*|\\d+)?\\.?(-.*)?");

    private final int majorVersion;
    private final int minorVersion;
    private final int bugFixVersion;
    private final String qualifier;
    private final String versionString;

    public SoftwareVersion(final String version)
    {

        this.versionString = version;
        Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);
        if (versionMatcher.find())
        {

            majorVersion = decode(versionMatcher, 1, 0);
            minorVersion = decode(versionMatcher, 2, 0);

            String group3 = versionMatcher.group(3);

            if (group3 != null && !group3.contains("-"))
            {
                bugFixVersion = decode(versionMatcher, 3, 0);
                qualifier = getOrElse(versionMatcher, 4, "").replaceFirst("-", "");
            }
            else
            {
                bugFixVersion = 0;
                qualifier = getOrElse(versionMatcher, 3, "").replaceFirst("-", "");
            }
        }
        else
        {
            throw new IllegalArgumentException("The version string is not in the expected format");
        }
    }

    public SoftwareVersion(final int majorVersion, final int minorVersion, final int bugfixVersion, final String qualifier)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.bugFixVersion = bugfixVersion;
        this.qualifier = qualifier;
        this.versionString = String.format("%d.%d.%d-%s", majorVersion, minorVersion, bugfixVersion, qualifier);
    }

    public SoftwareVersion(final int majorVersion, final int minorVersion, final int bugfixVersion)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.bugFixVersion = bugfixVersion;
        this.qualifier = "";
        this.versionString = String.format("%d.%d.%d", majorVersion, minorVersion, bugfixVersion);
    }


    public SoftwareVersion(final int majorVersion, final int minorVersion, final String qualifier)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.bugFixVersion = 0;
        this.qualifier = qualifier;
        this.versionString = String.format("%d.%d-%s", majorVersion, minorVersion, qualifier);
    }

    public SoftwareVersion(final int majorVersion, final int minorVersion)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.bugFixVersion = 0;
        this.qualifier = "";
        this.versionString = String.format("%d.%d", majorVersion, minorVersion);
    }

    private int decode(Matcher versionMatcher, int i, int defaultVal)
    {
        if (versionMatcher.group(i) != null)
        {
            return Integer.decode(versionMatcher.group(i));
        }
        return defaultVal;
    }

    private String getOrElse(Matcher versionMatcher, int i, String defaultVal)
    {
        if (versionMatcher.group(i) != null)
        {
            return versionMatcher.group(i);
        }
        return defaultVal;
    }

    public int getMajorVersion()
    {
        return majorVersion;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    public int getBugFixVersion()
    {
        return bugFixVersion;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    /**
     * Returns true if this version is greater than if equal to the specified version
     *
     * @param that the specified version to compare against
     * @return true if this version is greater than if equal to the specified version
     */
    public boolean isGreaterThanOrEqualTo(SoftwareVersion that)
    {
        return compareTo(that) >= 0;
    }

    /**
     * Returns true if this version is less than or equal to the specified version
     *
     * @param that the specified version to compare against
     * @return true if this version is less than or equal to the specified version
     */
    public boolean isLessThanOrEqualTo(SoftwareVersion that)
    {
        return compareTo(that) <= 0;
    }

    /**
     * Returns true if this version is greater than the specified version
     *
     * @param that the specified version to compare against
     * @return true if this version is greater than to the specified version
     */
    public boolean isGreaterThan(SoftwareVersion that)
    {
        return compareTo(that) > 0;
    }

    /**
     * Returns true if this version is less than the specified version
     *
     * @param that the specified version to compare against
     * @return true if this version is less than to the specified version
     */
    public boolean isLessThan(SoftwareVersion that)
    {
        return compareTo(that) < 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SoftwareVersion that = (SoftwareVersion) o;

        return compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + bugFixVersion;
        result = 31 * result + qualifier.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return versionString;
    }

    @Override
    public int compareTo(final SoftwareVersion that)
    {
        if (majorVersion < that.majorVersion)
        {
            return -1;
        }
        else if (majorVersion > that.majorVersion)
        {
            return 1;
        }

        if (minorVersion < that.minorVersion)
        {
            return -1;
        }
        else if (minorVersion > that.minorVersion)
        {
            return 1;
        }

        if (bugFixVersion < that.bugFixVersion)
        {
            return -1;
        }
        else if (bugFixVersion > that.bugFixVersion)
        {
            return 1;
        }

        if (StringUtils.isEmpty(qualifier) && !StringUtils.isEmpty(that.qualifier))
        {
            return 1;
        }
        else if (!StringUtils.isEmpty(qualifier) && StringUtils.isEmpty(that.qualifier))
        {
            return -1;
        }

        return qualifier.compareTo(that.qualifier);
    }
}

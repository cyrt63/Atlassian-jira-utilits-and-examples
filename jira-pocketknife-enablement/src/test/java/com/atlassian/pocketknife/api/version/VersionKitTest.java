package com.atlassian.pocketknife.api.version;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class VersionKitTest
{
    SoftwareVersion noQualifier;
    SoftwareVersion withQualifier;
    SoftwareVersion qualifierNoBug;

    @Before
    public void setUp() throws Exception
    {
        noQualifier = new SoftwareVersion(5,2,1);
        withQualifier = new SoftwareVersion(5,2,1,"OD-3");
        qualifierNoBug = new SoftwareVersion(5,2,"OD-3");
    }

    @Test
    public void testParseMajorLesser() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.0");
        assertThat(test, lessThan(noQualifier));
        assertThat(test, lessThan(withQualifier));
        assertThat(test, lessThan(qualifierNoBug));
    }

    @Test
    public void testParseMajorGreater() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("6.0");
        assertThat(test, greaterThan(noQualifier));
        assertThat(test, greaterThan(withQualifier));
        assertThat(test, greaterThan(qualifierNoBug));
    }

    @Test
    public void testParseMinorLesser() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.1");
        assertThat(test, lessThan(noQualifier));
        assertThat(test, lessThan(withQualifier));
        assertThat(test, lessThan(qualifierNoBug));
    }

    @Test
    public void testParseMinorGreater() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.3");
        assertThat(test, greaterThan(noQualifier));
        assertThat(test, greaterThan(withQualifier));
        assertThat(test, greaterThan(qualifierNoBug));
    }

    @Test
    public void testParseBugGreater() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.2.2");
        assertThat(test, greaterThan(noQualifier));
        assertThat(test, greaterThan(withQualifier));
        assertThat(test, greaterThan(qualifierNoBug));
    }

    @Test
    public void testParseBugLesser() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.2.0");
        assertThat(test, lessThan(noQualifier));
        assertThat(test, lessThan(withQualifier));
        assertThat(test, greaterThan(qualifierNoBug));
    }

    @Test
    public void testQualifierLesser() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.2-OD-3");
        assertThat(test, lessThan(noQualifier));
        assertThat(test, lessThan(withQualifier));
        assertThat(test, equalTo(qualifierNoBug));
    }

    @Test
    public void testQualifierGreater() throws Exception
    {
        SoftwareVersion test = VersionKit.parse("5.3.1-OD-3");
        assertThat(test, greaterThan(noQualifier));
        assertThat(test, greaterThan(withQualifier));
        assertThat(test, greaterThan(qualifierNoBug));
    }

    @Test
    public void testQualifierOnlyComparisons() throws Exception
    {
        SoftwareVersion smaller = VersionKit.parse("5.2.1-OD-2");
        assertThat(smaller, lessThan(withQualifier));
        SoftwareVersion greater = VersionKit.parse("5.2.1-OD-4");
        assertThat(greater, greaterThan(withQualifier));

        SoftwareVersion smaller2 = VersionKit.parse("5.2-OD-2");
        assertThat(smaller2, lessThan(qualifierNoBug));
        SoftwareVersion greater2 = VersionKit.parse("5.2-OD-4");
        assertThat(greater2, greaterThan(qualifierNoBug));
    }
}

This library can be used to use Atlassian Cache version 2.0 in your plugin.
The implementation will return the JIRA CacheManager instance if available, otherwise creates an in memory instance to use.

Dependencies
------------

Required Dependencies:

<dependency>
    <groupId>com.atlassian.pocketknife</groupId>
    <artifactId>jira-pocketknife-enablement</artifactId>
    <version>${pocketknife.version}</version>
</dependency>
<dependency>
    <groupId>com.atlassian.pocketknife</groupId>
    <artifactId>jira-pocketknife-cache</artifactId>
    <version>${pocketknife.version}</version>
</dependency>


Required package import/exports:

NOTE: Not currently required as the library won't switch over to a JIRA provided CacheManager just yet (we'll have to
wait for a suitable cache lib version as well as JIRA version that bundles it)

<Export-Package>
    com.atlassian.cache;version="${cache.version}",
    com.atlassian.cache.memory;version="${cache.version}",
</Export-Package>

<Import-Package>
    com.atlassian.cache;version="[${cache.version},${cache.version}]",
    com.atlassian.cache.memory;version="[${cache.version},${cache.version}]",
</Import-Package>

Note:
Make sure the cache version matches the version bundled with this library' jar.
Due to incorrect naming of the library (*), you have to specify the exact cache version.


Usage
-----

By default we set a timeout of 30 minutes instead of a cache limit:

Cache<Integer, Calendar> calendarCache = cacheManager.<Integer, Calendar>newCacheBuilder(Calendar.class, "calendarCache")
    .expireAfterAccess(30, TimeUnit.MINUTES)
    .loader(new CalendarLoader(calendarDao, calendarAOMapper, workingTimeDao, workingTimeAOMapper, holidayDao, holidayAOMapper))
    .build();


*) JIRA 6.1.3 ships with 2.0-m2, current version is 2.0-m11 which comes before m2. 2.0 final would be before m2 too!

UPDATE
- So confusing with version of com.atlassian.cache on Maven repo https://maven.atlassian.com/content/repositories/atlassian-public/com/atlassian/cache/atlassian-cache-memory/
2.0/	Fri Jul 18 16:23:06 UTC 2014
2.0.1/	Thu Jul 24 19:07:17 UTC 2014
2.1.0/	Fri Jul 25 08:29:45 UTC 2014
2.4.2/	Tue Jul 15 11:40:40 UTC 2014
which is to pick after
2.0-m11/	Mon Jul 21 04:24:15 UTC 2014
- Answer: com.atlassian.cache:atlassian-cache-memory:jar:2.0
- Based on commit on https://bitbucket.org/atlassian/atlassian-cache/commits/all?page=9, v2.0 was released after 2.0-mx releases
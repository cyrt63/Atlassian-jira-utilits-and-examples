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
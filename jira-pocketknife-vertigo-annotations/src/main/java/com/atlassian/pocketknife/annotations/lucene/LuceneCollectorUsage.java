package com.atlassian.pocketknife.annotations.lucene;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for marking a class as using an {@link org.apache.lucene.search.Collector}, categorising (and if
 * necessary) clarifying it's use case.
 *
 * @since 0.72.0
 */
@Retention(RetentionPolicy.CLASS)
public @interface LuceneCollectorUsage {
    enum CollectorUsageType {
        IssueCollection,
        IssueAggregation,
        TimeSeriesCollection,
        TimeSeriesAggregation,
        Unknown
    }

    CollectorUsageType type();

    String comment() default "";
}
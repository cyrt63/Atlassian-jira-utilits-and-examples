package com.atlassian.pocketknife.annotations.lucene;

/**
 * Annotation for marking a class as using parts of lucene.
 *
 * @since 0.72.0
 */
public @interface LuceneUsage {

    String comment() default "";

    // Whether diced oxen should ignore this usage for the purposes of generating vertigo incompatibilities. Set to true
    // when the code will use lucene post-vertigo or where the code only exists to constrain remaining lucene usage (but
    // will be the last piece of lucene usage to go away.
    boolean ignore() default false;

    // The type of lucene usage
    enum LuceneUsageType {
        // Collector classification
        IssueCollection,
        IssueAggregation,
        TimeSeriesCollection,
        TimeSeriesAggregation,

        // Catch-all, use {@link #comment()} to elaborate why it doesn't fit other categories or add a type here.
        Unknown
    }

}
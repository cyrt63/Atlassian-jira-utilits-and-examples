package com.atlassian.pocketknife.annotations.lucene;

/**
 * Annotation for marking a class as using parts of Lucene.
 *
 * @since 0.72.0
 */
public @interface LuceneUsage {

    LuceneUsageType type();

    String comment() default "";

    /**
     * Whether diced oxen should ignore this usage for the purposes of generating Vertigo incompatibilities. Set to true
     * when the code will use Lucene post-Vertigo or where the code only exists to constrain remaining Lucene usage (but
     * will be the last piece of Lucene usage to go away.
     *
     * @return false by default
     */
    boolean ignore() default false;

    /**
     * The type of Lucene usage.
     */
    enum LuceneUsageType {
        // Collector classification
        IssueCollection,
        IssueAggregation,
        TimeSeriesCollection,
        TimeSeriesAggregation,
        IssuePropertyCollection,
        IssuePropertyAggregation,
        Indexer,
        SearchExtractor,
        FieldSelector,

        /**
         * @deprecated  Use SearchExtractor instead
         */
        @Deprecated
        IssueSearchExtractor,

        // Catch-all, use {@link #comment()} to elaborate why it doesn't fit other categories or add a type here.
        Unknown
    }

}

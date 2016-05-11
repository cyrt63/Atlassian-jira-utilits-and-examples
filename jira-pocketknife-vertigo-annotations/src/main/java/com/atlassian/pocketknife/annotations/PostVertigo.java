package com.atlassian.pocketknife.annotations;

/**
 * An annotation to mark cleanup activities or changes to be performed after Vertigo
 */
public @interface PostVertigo {

    /**
     * A descriptive comment of the change to be made
     */
    String comment();

}

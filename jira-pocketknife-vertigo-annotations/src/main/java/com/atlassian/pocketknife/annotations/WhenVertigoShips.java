package com.atlassian.pocketknife.annotations;

/**
 * An annotation to mark cleanup activities or changes to be performed after Vertigo ships in production.
 *
 * <p> Example of changes covered by this annotations:
 * <ul>
 *     <li>BTF-specific code that can be removed after Service Desk forks</li>
 *     <li>Nice-to-have features that will need to be postponed due to time constraints</li>
 *     <li>Code refactoring activities that we identify while working on Vertigo, but we don't have time to tackle
 *     immediately</li>
 *     <li>Hacks that we might have to introduce, but would like to cleanup afterwards</li>
 * </ul>
 *
 * <p> Changes on non-Java files (XML, JavaScript, etc) should be formalized as a comment prefixed by the
 * <strong>WhenVertigoShips</strong> identifier.
 */
@SuppressWarnings("unused")
public @interface WhenVertigoShips {

    /**
     * A descriptive comment of the changes to be made.
     */
    String value();

}

This set of libraries contains code that is useful to Atlassian Plugins.

Its first incarnation is all about JIRA plugins. The reason for this is that it was JIRA-based teams that first created this set of libraries.

Time will tell if it expands out into more useful things for other types of Atlassian Plugins.

The idea is to have a set of "targeted" libraries that do specific jobs.

We don't want any individual library to become a ghetto of code, nor do we want complex interdependencies between the libraries.

# Contribution #

All pull requests are expected to have a green branch build in the [Pocketknife](https://servicedesk-bamboo.internal.atlassian.com/browse/PK-PK) plan. Once the pull request is merged to master, you can manually release a version using the [Pocketknife Release](https://servicedesk-bamboo.internal.atlassian.com/browse/PK-PKRLS) plan.
# kaptein-api

Backend application for Kaptein, serving and crunching behandlinger for graphs.

# Linting and verification

This project uses ktlint and detekt for linting and static code analysis. See internal Confluence page for Team Klage for more info.

```
./gradlew ktlintFormat   # auto-fix formatting
./gradlew ktlintCheck    # verify formatting
./gradlew detektMain detektTest
```

detekt is scoped to the `NamedArguments` rule, which requires call sites with more
than one argument to name their arguments.

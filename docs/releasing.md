# Releasing

## Java modules (Maven)

1. Finalize `CHANGELOG.md`: rename `[Unreleased]` to the version being released with today's date
   (`## [X.Y.Z] - YYYY-MM-DD`), add a fresh empty `[Unreleased]` section above it, and add or
   update the compare links at the bottom of the file.
2. Set the parent `pom.xml`'s `<version>` to the release version (no `-SNAPSHOT`); the modules
   inherit it from the parent, so nothing else needs editing.
3. Commit: `Release X.Y.Z`.
4. Tag: `git tag -a vX.Y.Z -m "Release X.Y.Z"`.
5. Push the branch, then push the tag: `git push` and `git push origin vX.Y.Z`.
6. `.github/workflows/release.yml` runs on the tag push: it verifies the tag matches the pom
   version (failing otherwise), runs `mvn -B package -DskipTests`, extracts the version's
   `CHANGELOG.md` section into release notes, and publishes a GitHub Release with the main,
   sources and Javadoc jars for every module attached.
7. Confirm the release on GitHub has all the expected jars, then set the pom back to the next
   `-SNAPSHOT` version (`X.Y.(Z+1)-SNAPSHOT`), commit `Start X.Y.(Z+1)-SNAPSHOT development`, and
   push.

### Maven Central

**Status: not done.** Publishing to Maven Central (via the Central Publishing Portal, GPG-signed
artifacts, a `distributionManagement` block, and the `central-publishing-maven-plugin` or
equivalent) is planned but not yet set up. Until it is, install locally with `mvn install` (see
the [README](../README.md#install)).

## `avl-report` (npm)

`avl-report`'s `package.json` version is kept in lockstep with the Java modules' pom version and
released from the same `vX.Y.Z` tag: bump it in the same "Release X.Y.Z" commit as step 2 above.

**Status: not done.** Publishing to npm (`npm publish` from `avl-report/`, with an npm account
and an access token configured in CI) is planned but not yet set up. Until it is, run it from a
checkout (see [avl-report/README.md](../avl-report/README.md#install)).

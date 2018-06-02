## 0.1.9

__2018-06-02__

- Fix: fix can't find createDebugCoverageReport issue when there are flavors exists

## 0.1.8

__2018-05-23__

- Feat: handle the case of android gradle plugin 3.2.0 for findbugs
- Feat: integrate coverageReport for unitTest and androidTest and just disabled it as default. closes #2
- Feat: change the coverageReport extension pattern. refs #2
- Fix: fix can't create passed module file issue
- Fix: fix test dir is exist issue

## 0.1.7

__2018-05-07__

- Fix: fix can't find assemble task when execute okFindbugs for non-android project

## 0.1.6

__2018-05-06__

- Fix: fix can't find testUnitTest task issue when run okcheck for all variants
- Fix: fix can't find okLint task when lint task has already been added during require add okLint task

## 0.1.5

__2018-05-02__

- Fix: fix oklint add flavor and debug malposition issue
- Fix: fix can't find oklint on okcheck task issue if there are flavor(s) on the project
- Fix: fix can't find unitTest task on the okcheck task issue if there are flavor(s) on the project

## 0.1.4

__2018-05-01__

- Feat: split tasks with build-type and flavor
- Feat: support define the custom config for checkstyle, findbugs and pmd
- Feat: integrate unit-test on okcheck
- Feat: support incremental check locally for pmd, lint, ktlint, okcheck, (findbugs and checkstyle incremental by offical support)
- Feat: apply sub plugin only if it is enabled, because we provide sub-scope extension now
- Feat: support using latest success commit id as the target compare id if there isn't any okcheck on the current branch
- Feat: using built-in extension to make config more readable, such as okcheck `{ findbugs {..} unittest {..} .. }`
- Feat: support config the version of ktlint through ktlint scope on okcheck

## 0.1.3

- Feat: using debug type instead for both library and application
- Feat: using the .okcheck cache on the current path when it exist

## 0.1.2

- Feat: using the new pmd rulest to ignore single-line-if without braces and some others, more detail please see: `src/main/resources/pmd-ruleset.xml`

## 0.1.1

- Feat: support exclude param on the okcheck extension
- Feat: handle the case of using ignoreOkcheckDiff on target-module okcheck task

## 0.1.0

- Feat: support `ignoreOkcheckDiff` to check whole modules, such as `./gradlew -PignoreOkcheckDiff okcheck`
- Feat: check all modules when there isn't ~/.okcheck exist
- Feat: add `cleanOkcheckDiff` task to clean the okcheck diff folder on ~/.okcheck

## 0.0.9

- Feat: exclude protobuf
- Fix: fix special extension with pmd,findbugs,checkstyle can't effect

## 0.0.8

- Fix: fix SuppressionFilter can't on Tree error

## 0.0.7

- Feat: enable picasso style rules as default and split rules to two type: with-suppressions if rootProject/suppressions.xml exist or without-suppressions if it is not exist

## 0.0.6

- Feat: support destination field on the extension to judge the report parent folder, and as default will output the reports on the build dir of root project

## 0.0.5

- Feat: support lint,ktlint,checkstyle,pmd,findbugs on okcheck

## 0.0.4

- Fix: fix commit-id is dir issue when there is run okcheck command not on the git repo
- Fix: fix do okcheck job on non okcheck task.

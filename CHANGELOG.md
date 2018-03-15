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

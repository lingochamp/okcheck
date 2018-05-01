# OkCheck

![](https://img.shields.io/badge/OkCheck-Increamental-green.svg)
![](https://img.shields.io/badge/OkCheck-Lint UnitTest-orange.svg)
![](https://img.shields.io/badge/OkCheck-KtLint Checkstyle Findbugs Pmd-yellow.svg)
[ ![Download](https://api.bintray.com/packages/jacksgong/maven/OkCheck/images/download.svg) ](https://bintray.com/jacksgong/maven/OkCheck/_latestVersion)
[![](https://img.shields.io/badge/SnapShot-0.1.5-white.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/liulishuo/okcheck/)

Incremental scan，integrate Lint、KtLint、UnitTest、Checkstyle、Findbugs、Pmd, powerful and easy to use.

> [中文文档](https://github.com/lingochamp/okcheck/blob/master/README-zh.md)

## Basic Incremental Scan

- Base on the Git version control, compare to the latest success scan => Only scan the changed module
- Base on the local build cache => If there isn't any change compare to the last scan tasks will finish with `up-to-data` as far as possible

## Example

![](https://github.com/lingochamp/okcheck/raw/master/art/diff.jpg)
![](https://github.com/lingochamp/okcheck/raw/master/art/reports.png)
![](https://github.com/lingochamp/okcheck/raw/master/art/up-to-date.jpg)

## How to Import

On the `build.gradle` at your root project:

```groovy
buildscript {
    dependencies {
        classpath 'com.liulishuo.okcheck:gradle:{latest_version}'
    }
}

allprojects {
    apply plugin: 'okcheck'
}
```

Done! Everything is ready to scan, now you can check with 6 job just run `./gradlew okcheckDebug` and just see result, and all report is settle down the `build/reports` for the root project as default.

> At the present, we are waiting for accepting of jcenter for release version, so you can use snapshot version first.

There is Snapshot version also valid, if you want to use snapshot version, please add repository on the `build.gradle`:

```groovy
buildscript {
  repositories {
      maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
  }
}
```

## Task Description

When you run `okcheck` task, we will compile and check with all `variant`(As usual, there are two `variant` as `debug` and `release`), so, normally you just need to check with one `variant` to increase scan speed, such as `okcheckDebug`.

![](https://github.com/lingochamp/okcheck/raw/master/art/tasks.jpg)

- `./gradlew okcheckDebug`: Run the okcheck task for the Debug build type.
- `./gradlew cleanOkcheckDiff`: Clean all cached success commit id(which is on the `~/.okcheck` folder as default), since then the next time we will scan all module.
- `./gradlew -PignoreOkcheckDiff okcheck`: Run the okcheck task and ignore cached success commit id, which will raise run okcheck for whole module.
- `./gradlew -PignoreOkCheckDiff :module1:okcheck`: Run the okcheck task for the module1 and ignore its cached success commit id, which will raise run okcheck certainly even if there isn't any change compare to last success scan.

## How to Customize

For the convenient, all value on the bellow is the default value as example, the follow code is write on the `build.gradle` at the root project.

```
allprojects {
    apply plugin: 'okcheck'

    okcheck {
        exclude = ['**/proto/*.java']
        destination = project.rootProject.buildDir

        unittest {
            enabled = true
            exclude = ['**/proto/*.java']
        }
        lint {
            enabled = true
            exclude = ['**/proto/*.java']

        }
        ktlint {
            enabled = true
            exclude = ['**/proto/*.java']
            version = "0.22.0"
        }
        checkstyle {
            enabled = true
            exclude = ['**/proto/*.java']
            // We will use the default config build-in the okcheck
            configFile = null
        }
        findbugs {
            enabled = true
            exclude = ['**/proto/*.java']

            effort = "default"

            // Whether allow the build to continue if there are warnings
            ignoreFailures = false
            // We will use the default excludeBugFilter file build-in the okcheck
            excludeBugFilter = null
        }
        pmd {
            enabled = true
            exclude = ['**/proto/*.java']

            // Whether allow the build to continue if there are warnings
            ignoreFailures = false
            // We will use the default ruleSetFiles build-in the okcheck
            ruleSetFiles = null
        }
    }
}
```


## Others

- As you know, the first time of running okcheck will check all module as default

#### Exclude

Following paths are exclude on `checkstyle`, `findbugs`, `pmd` as default:

```
**/gen/**
**/test/**
**/proto/*.java
**/protobuf/*.java
**/com/google/**/*.java
```

## LICENSE

```
Copyright (c) 2018 LingoChamp Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

# OkCheck

![](https://img.shields.io/badge/OkCheck-Increamental-green.svg)
![](https://img.shields.io/badge/OkCheck-Lint%20UnitTest-orange.svg)
![](https://img.shields.io/badge/OkCheck-KtLint%20Checkstyle%20Findbugs%20Pmd-yellow.svg)
[ ![Download](https://api.bintray.com/packages/jacksgong/maven/OkCheck/images/download.svg) ](https://bintray.com/jacksgong/maven/OkCheck/_latestVersion)
[![](https://img.shields.io/badge/SnapShot-0.1.6-white.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/liulishuo/okcheck/)

差量扫描，自动集成Lint、KtLint、UnitTest、Checkstyle、Findbugs、Pmd 强大且灵活的Android Gradle插件

> [English](https://github.com/lingochamp/okcheck)

## 基本差量扫描

- 基于Git仓库，对比最近一次扫描成功的记录commit id => 扫描差量的module
- 基于本地代码, 对比最近一次扫描成功的缓存 => 已经处理的任务直接`up-to-data`

## 案例

![](https://github.com/lingochamp/okcheck/raw/master/art/diff.jpg)
![](https://github.com/lingochamp/okcheck/raw/master/art/up-to-date.jpg)
![](https://github.com/lingochamp/okcheck/raw/master/art/reports.png)

## 如何引入

在根项目的`build.gradle`中配置:

```groovy
buildscript {
    dependencies {
        classpath 'com.liulishuo.okcheck:gradle:0.1.6'
    }
}

allprojects {
    apply plugin: 'okcheck'
}
```

至此，就已经完全整合，并且采用我们定制的统一规则生效5大静态扫描工具与单元测试，以及可以通过`./gradlew okcheck`差分静态扫描，并且默认所有报告会整合到根项目的`build/reports`目录下，方便统一导出(但是如果你想要报告存在原本的目录，只需要将`destination`设置为`project.buildDir`即可)。

如果你希望使用`Snapshot`版本，请添加以下仓库到你的`gradle.build`:

```groovy
buildscript {
  repositories {
      maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
  }
}
```

## 任务说明

当进行`okcheck`任务的时候，会对所有的`variant`进行编译与扫描（如有一般都有`debug`与 `release`两个编译类型导致，存在至少两个`variant`)，因此通常来说我们只需要对某一个`variant`进行编译扫描即可: 如`okcheckDebug`。

![](https://github.com/lingochamp/okcheck/raw/master/art/tasks.jpg)

- `./gradlew okcheckDebug`: 执行差量的扫描
- `./gradlew cleanOkcheckDiff`: 清除所有缓存的差量数据，下次会全量扫描
- `./gradlew -PignoreOkcheckDiff okcheck`: 忽略差量数据进行全量的扫描
- `./gradlew -PignoreOkCheckDiff :module1:okcheck`: 忽略差量数据进行`module1`模块的扫描

## 如何配置

为了方便说明下面所有提到的，都采用默认值(当没有提供时所采用的默认值)作为案例，以下是在根项目的`build.gradle`中配置:

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
            // 采用默认的统一配置
            configFile = null
        }
        findbugs {
            enabled = true
            exclude = ['**/proto/*.java']

            effort = "default"

            // Warning级别的错误默认会终止扫描
            ignoreFailures = false
            // 采用默认的统一配置
            excludeBugFilter = null
        }
        pmd {
            enabled = true
            exclude = ['**/proto/*.java']

            // Warning级别的错误默认会终止扫描
            ignoreFailures = false
            // 采用默认的统一配置
            ruleSetFiles = null
        }
    }
}
```


## 其他

- 首次执行`okcheck`任务会进行全量扫描
- 如果你希望针对`checkstyle`任务`suppress`一些方法与行，对于默认内置的`checkstyle`的配置，有只需要在根项目目录添加`suppressions.xml`文件，并在里面定义它们即可

#### 已经忽略包

在`checkstyle`,`findbugs`,`pmd`中忽略了以下路径的扫描:

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

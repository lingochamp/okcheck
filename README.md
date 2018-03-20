# Okcheck

差量扫描，自动集成Lint、KtLint、Checkstyle、Findbugs、Pmd 5大互补静态扫描工具，灵活配置

## 如何引入

在根项目的`build.gradle`中配置:

```groovy
buildscript {
    dependencies {
        classpath 'com.liulishuo.okcheck:gradle:0.1.2'
    }
}

allprojects {
    apply plugin: 'okcheck'
}
```

至此，就已经完全整合，并且采用我们定制的统一规则生效5大静态扫描工具，以及可以通过`./gradlew okcheck`差分静态扫描，并且默认所有报告会整合到根项目的`build/reports`目录下，方便统一导出。

## 如何配置

如下配置不引入KtLint与Checkstyle，让报告导出到每个module自己的reports目录下，以及`checkstyle`,`pmd`,`findbugs`忽略路径包含`protobuf`的所有`java`文件:

```
allprojects {
    okcheck {
        // AndroidLint 会强制开启，所以目前没有提供开关
        enableCheckstyle = false
        // enableFindbugs = false
        // enablePmd = false
        enableKtlint = false

	// 为了方便统一导出，因此默认的所有报告会在根项目的`build/reports/<modules>/`下面
	// 如果想要定制到每个独立module自己的reports下面，使用如下配置
	destination = buildDir

	//`checkstyle`,`pmd`,`findbugs`忽略路径包含`protobuf`的所有`java`文件
	exclude = ['**/protobuf/*.java']
    }
}
```

配置让编译继续，如果findbugs扫描出存在warnnings的错误(其他的同理):

```
subprojects {
    findbugs {
        // 默认值是default，最大值是max，最小值是min，用于调节扫描精度，精度越高扫描到的问题越多
        effort = "max"
        ignoreFailures = true
    }
}
```

配置忽略`NeedIgnoreDemo.java`

任务说明:

- `./gradlew okcheck`: 执行差量的扫描
- `./gradlew cleanOkcheckDiff`: 清除所有缓存的差量数据，下次会全量扫描
- `./gradlew -PignoreOkcheckDiff okcheck`: 忽略差量数据进行全量的扫描
- `./gradlew -PignoreOkCheckDiff :module1:okcheck`: 忽略差量数据进行`module1`模块的扫描

## 其他

- 在`apply`了`okcheck`以后，如若没有对源码与资源扫描的任务(`lint`,`ktlint`,`checkstyle`,`pmd`)进行关闭，默认也会将这几个任务绑定到`check`任务中。
- 首次执行`okcheck`任务会进行全量扫描

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

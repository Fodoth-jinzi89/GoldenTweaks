# libs —— 本地编译依赖（不随仓库分发）

本目录存放编译期需要的第三方模组 jar，由根目录 `build.gradle` 通过 `fileTree` 与 `flatDir` 引入：

| 子目录 | Gradle 配置 | 说明 |
|---|---|---|
| `compileOnly/` | `compileOnly fileTree(...)` | 仅编译期可见，不会进运行期 classpath |
| `implementation/` | `implementation fileTree(...)` | 编译期与运行期都可见 |
| `runtimeOnly/` | `runtimeOnly fileTree(...)` | 仅运行期可见（目录可不存在） |
| `reference/` | 无 | 仅供人工查阅的反编译参考 jar，不参与编译 |

这些 jar 的版权归各自模组作者所有，因此 **不随本仓库分发**，仓库里该目录只保留本说明文件。

要让 `./gradlew build` 通过，请自行准备对应版本的模组 jar 并按上表放入子目录：`fileTree`
会收集整个目录下的所有 `*.jar`，文件名不重要，但版本必须与源码引用的 API 对应；缺失的类会在
`compileJava` 阶段以 `cannot find symbol` 报错暴露出来。

> 注意：不要把同一模组的多个版本同时放进同一个子目录，否则可能出现重复类冲突。

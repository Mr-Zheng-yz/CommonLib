# CommonLib

Android 公共组件库，通过 [JitPack](https://jitpack.io) 发布，包含三个可独立依赖的库模块。

## 模块介绍

### common-libs

基础公共模块，其他两个库模块都依赖它。主要功能：

- `LogUtil` / `FileLogger`：日志工具，支持写入文件、缓冲队列、按时间清理旧日志
- `LoadingDialogFragment`：轻量加载等待弹窗，支持超时自动关闭
- `MultiStateLayout`：多状态布局（加载中 / 空数据 / 错误 / 内容）
- `UiHelper`、`SPUtil`、`TimeUtil`、`Base64Utils`、`NetworkMonitor`、`ThreadPoolManager` 等常用工具类
- `BridgeCore`：JS Bridge 桥接核心
- 防重复点击系列监听器（`OnSingleClickListener`、`DelayTapClickListener` 等）

### mq-lib

RabbitMQ 封装模块，基于 `com.rabbitmq:amqp-client`。

- `RabbitMqManager`：单例管理类，提供连接 / 断开、队列订阅、消息发布、连接状态监听、自动重连等能力
- 通过 `RabbitMqManager.Config.Builder` 配置连接参数

### mqtt-lib

MQTT 封装模块，基于 `org.eclipse.paho:org.eclipse.paho.client.mqttv3`。

- `MQTTManager` / `MQTTManagerAndroid`：单例管理类，提供连接、订阅、发布、断线 5 秒间隔自动重连等能力
- 通过 `init(MqttEntity, MQTTListener)` 初始化

## Gradle 依赖方式

在使用方项目的 `settings.gradle` 中添加 JitPack 仓库：

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://www.jitpack.io' }
        google()
        mavenCentral()
    }
}
```

然后按需引入模块（`Tag` 为发版版本号，如 `1.0`）：

```groovy
// 只依赖基础模块
implementation 'com.github.Mr-Zheng-yz.CommonLib:common-libs:Tag'

// 依赖 RabbitMQ 模块（自动传递带入 common-libs）
implementation 'com.github.Mr-Zheng-yz.CommonLib:mq-lib:Tag'

// 依赖 MQTT 模块（自动传递带入 common-libs）
implementation 'com.github.Mr-Zheng-yz.CommonLib:mqtt-lib:Tag'
```

说明：

- mq-lib / mqtt-lib 内部以 `api` 方式依赖 common-libs，已写入各自 POM，使用者无需再显式声明 common-libs。
- 若同时显式依赖 common-libs 和 mq-lib / mqtt-lib，Gradle 按坐标去重，版本冲突时取最高版本，不会产生重复类。
- 排查线上崩溃时需要用 `build/outputs/mapping/release/mapping.txt` 还原堆栈，建议每次发版保存该文件
- SNAPSHOT 调试：发正式 tag 前，如果想先验证使用效果，可以不升级版本号，直接让使用方依赖最新快照 `master-SNAPSHOT` （JitPack 会按 master 分支最新提交构建），验证通过后再走上面的正式发版流程。如：`com.github.Mr-Zheng-yz.CommonLib:mq-lib:master-SNAPSHOT`

## 发版升级步骤

版本号统一在根目录 `gradle.properties` 的 `RELEASE_VERSION` 中维护，**必须与 git tag 保持一致**。

1. **修改版本号**：编辑 `gradle.properties`，如 `RELEASE_VERSION=1.1`
2. **提交并推送代码**：

   ```bash
   git add -A
   git commit -m "release 1.1"
   git push
   ```

3. **打 tag 并推送**（tag 名与 `RELEASE_VERSION` 相同）：

   ```bash
   git tag 1.1
   git push origin 1.1
   ```

   或在 GitHub 网页上基于最新提交创建 Release。
4. **触发 JitPack 构建**：打开 `https://jitpack.io/#Mr-Zheng-yz/CommonLib`，输入 tag 点 "Look up" → "Get it"，查看构建日志确认成功。不主动触发时，首个使用者请求该版本也会自动构建（首次下载需等待）。
5. **使用方升级**：将依赖中的版本号改为新版本即可。

## 本地验证发布产物

JitPack 云端构建本质上是执行 `publishToMavenLocal`，本地可用同样方式提前验证，避免发版后才发现问题：

```bash
./gradlew :common-libs:publishReleasePublicationToMavenLocal \
          :mq-lib:publishReleasePublicationToMavenLocal \
          :mqtt-lib:publishReleasePublicationToMavenLocal
```

验证点：

1. **产物是否生成**，查看 `~/.m2/repository/com/github/Mr-Zheng-yz/CommonLib/` 下各模块目录，应包含 `.aar`、`.pom`、`.module` 文件：

   ```bash
   ls ~/.m2/repository/com/github/Mr-Zheng-yz/CommonLib/mq-lib/<版本号>/
   ```

2. **POM 传递依赖是否正确**，mq-lib / mqtt-lib 的 POM 中应包含 common-libs 依赖：

   ```bash
   grep -A 3 "<dependency>" ~/.m2/repository/com/github/Mr-Zheng-yz/CommonLib/mq-lib/<版本号>/mq-lib-<版本号>.pom
   ```

   预期能看到：

   ```xml
   <dependency>
     <groupId>com.github.Mr-Zheng-yz.CommonLib</groupId>
     <artifactId>common-libs</artifactId>
     <version>1.0</version>
   </dependency>
   ```

3. **版本号是否与 `RELEASE_VERSION` 一致**（POM 中的 `<version>` 与目录名）。

注意：若依赖解析异常，可加 `--refresh-dependencies` 强制刷新缓存重试。

## 环境要求

- JDK 17（Gradle 8.9 + AGP 8.7.3 要求，JitPack 云端通过根目录 `jitpack.yml` 指定）
- compileSdk 34，minSdk 24

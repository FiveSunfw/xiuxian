# 协作开发

本项目是 Minecraft Java 1.20.1 / Forge 47.x 模组，使用 Java 17 和 Gradle Wrapper。当前 `main` 已包含 0.3.7 炼器装备与资源。新任务从 `main` 创建功能分支，验证后再合并回 `main`。仓库目前没有远端地址。

## 获取与构建

克隆仓库后，使用 Java 17。Windows 下可将 JDK 解压到项目的 `.tools/jdk-17...`，或设置 `JAVA_HOME`。运行 `./build.ps1 build` 构建；Gradle Wrapper 和项目目录内的 `.gradle-cache` 会管理依赖缓存。生成的 JAR 位于 `build/libs/`。项目里的 JDK、缓存、测试世界和 JAR 不提交到 Git。

自动游戏测试可运行 `./build.ps1 runGameTestServer`。客户端实际手感和画面由试玩者确认；自动测试结果与实机结果分别写入 `VALIDATION.md`。

## 分支与提交

从当前开发分支创建自己的功能分支，完成一项功能后提交源码、配方、贴图、翻译以及对应文档。提交前查看 `git status` 和 `git diff --check`，确认没有缓存、客户端存档或无关文件。不要直接覆盖别人的工作，也不要把本机绝对路径写进项目配置。

每完成开发任务，更新 `DEVELOPMENT.md` 的状态和任务记录；实际执行的检查写入 `VALIDATION.md`。需要联机功能时，客户端和服务端使用同一版本 JAR。

如需共享到 GitHub 或其他平台，由仓库维护者先提供远端仓库地址，再设置 `origin`、推送分支并通过合并请求协作。

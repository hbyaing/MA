# 换电脑接续：自由炼丹V5

仓库：https://github.com/thezinecraftm-rgb/Myriad-Ascension.git，默认分支 main。先读根 AGENTS.md，检查分支/远程/未提交修改，fetch 后安全同步最新 main，再开功能分支。不能直接覆盖已有工作；不要使用 reset --hard 或强推。

## 当前进度

- 第01项规范/基线完成；第02项实际原料观察面板完成，完整V5面板待后续。
- 第03项纯反应核心完成：提交 `2a11c5e`，PR47已合并，合并提交 `b65be8051d1f477ac64fae2565001bd2643f7463`。
- 第04项已实现真实输入、环境/异火轨迹、药材去品阶与创造模板规则、基础蕴月瓶；验证和限制见 docs/testing/ALCHEMY_V5_INPUTS_20261027.md。第05至13项尚未完成；下一步是实体炉批次事务。
- V5核心尚未由实际炉体调用；游戏仍用legacy炼丹结算。不要把工作台、纯核心或专服核心夹具当成实际炼丹接入。

最新设计：[DESIGN_V5_0_R1.txt](../specifications/alchemy-v5/DESIGN_V5_0_R1.txt)，包含原稿全文和本次追加修订，原始V5.0单独保留。SHA256（Git仓库LF字节，换机校验先统一LF） `8350e80da997b007f2c101adca774847ec43fbdd79b86c926c65afcea3e91e39`。
制作及新增子任务：[ALCHEMY_V5_CHECKLIST.md](ALCHEMY_V5_CHECKLIST.md)。旧物品具体折算仍仅有审阅方案，未批准，不执行破坏性转换。

## 本次新增设计要点

夜晚/月相分别影响连续环境参数和条件标签，同一来源不能重复加药力；天亮从实际变化步更新，不追溯取消已完成反应。连续窗口中断则重新计时，已吸收资源按真实规则保留/消耗，结构按维持条件演化。跨夜路线允许有代价温养，不保证所有阶段可暂停。

蕴月瓶存有限月华及来源，可白天供料，不能伪造当前夜晚/满月天象。参数/标签/响应与显示数据驱动，规则白名单执行。容量、速率、损耗、配方、美术未定；这些提议本次仅更新文档，未注册道具或改游戏代码。

## 接续入口和验证

- Java17、Minecraft1.20.1、Forge47.4.10、modId=wuxingxiantu；换机重新核对构建，不迁移加载器。
- 先读 accepted-decisions.json、accepted-plan.md、ALCHEMY_REWORK_PROPOSAL_20261027.md、docs/previews/alchemy-rework/README.md，以及核心规格 docs/testing/ALCHEMY_V5_CORE_20261027.md。
- 核心在 `src/main/java/cn/blockforge/generated/generatedmod/alchemy/simulation/`；正式 AlchemyService/炉体/天地为炉为后续接入点。参数观察层在 alchemy/observation 和 client/AlchemyObservationView。
- 上次 verifyFull 构建通过、GameTests编译通过且实际364项全通过，新增纯Java388项通过；网页10247项额外回归通过。历史通过不代表换机最新代码整体验收。
- 正常命令核对后用 `.\gradlew.bat verifyFull`；Python/Java本机路径按新电脑配置，不照搬旧绝对路径。完整专服验收不能启用 myriadExternalShaders。
- 网页通过仓库工作台总入口启动，不能复用旧机器127.0.0.1地址；根AGENTS指定路径不可用时核对仓库便携入口 docs/previews/启动全部工作台.py。网页不是游戏结算。
- 专服、JEI/光影、多人、自然生成/旧档等仍按各阶段真实验收；原恢复包独立克隆问题记录保留，不因源码可见就宣称恢复包已验收。

## 旧电脑磁盘状态（不是新电脑路径）

正式包 Myriad-Ascension-2a11c5e.jar 已安装到用户指定 D:/我的世界/.minecraft/versions/1.20.1-Forge_47.4.26/mods；SHA256 `5550bf6f3791e4a13a423f9dba79ebdc70652c4efe368ccd8ffdefee5bebe695`。旧包已备份，其余18个Mod哈希未变；运行中的JVM须重启加载。此次文档修订没有新JAR，也不执行安装。

本地 deliverables、reports、Gradle缓存和工作台服务器进程并非全部跟随Git上传。需要新包时在新机器构建；不要声称这些本地文件已同步。

## 2026-10-27 第04项本机接续

本次功能分支 codex/alchemy-v5-inputs-20261027，基线main 3c571f7。第04项实现和限制详见 docs/testing/ALCHEMY_V5_INPUTS_20261027.md；最新 clean verifyFull 实际374项必需服务端测试全通过，警告119/119。原AXO实例发现失败后，用户指定PCL目录；已安装到 E:/我的世界/PCL 正式版 2.10.9/.minecraft/versions/1.20.1-Forge_47.4.26/mods，正式JAR SHA256见本项规格。实际PCL客户端20张创造页+取物NBT验收通过，测试进程正常保存退出，临时保护包已移除，保留GeckoLib和Moonlight依赖。下一项05开始实际炉体事务，继续保护旧物品数据，不能误以为当前炉体已调用V5。

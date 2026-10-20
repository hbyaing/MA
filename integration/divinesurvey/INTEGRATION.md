# D25 矿物与结构探查接线

独立 `divinesurvey` 包已经完成，自动 Forge subscribers 仅在会话启动后工作。未修改总注册、协议表、既有神识动作或识海页。

根任务接线：
1. `NetworkChannel` 新增 S2C `SurveyPacket`，构造器 `SurveyPacket::new`、`SurveyPacket::encode`、`SurveyPacket::handle`，更新协议ID表与版本。
2. `GeneratedMod` 注册 `ModConfig.Type.CLIENT` 的 `SurveyClientConfig.SPEC`（内容无 Minecraft 客户端类型，可在专用服务端配置注册代码中引用）。Xaero结构点导出默认关闭。
3. `SpiritualSense` 增加只读 `public static boolean channeling(ServerPlayer p){var s=ACTIVE.get(p.getUUID());return s!=null&&s.channel!=0;}`，供新增模块自身验证凝神状态。`SpiritualSense.action` 允许 `survey`，在既有 channel 凝神检查之后、现有 scan/identify/lock 分支之前分发 `DivineSurvey.toggle(p); return;`。不改变 lock/scan 原逻辑。关闭可以允许凝神时进行，避免无法停用。共用既有5tick动作限流。
4. 识海页使用现有 CultivationUi.Action 增加“矿物/结构探查 · 化神”切换按钮，发送现有 `ProgressionActionPacket("survey")`。本地 `SurveyClient.active()` 可显示开/关；数据权威仍在服务端。避免覆盖现有锁定按钮与说明。

门槛：五行realm>=4且既有SenseOpen。开启扣既有Sense 8点，维持每秒1点，30秒自动复查另扣8点；移动超过2区块重新居中，不再额外收费。保留五行神识回能规则，故回能可抵消维持消耗；未增加第二资源树。自身检查麻痹、出窍，启动检查正在凝神；死亡、降境、关闭识海、观战、退出、换维度、重生、服务器停止均释放票据/会话；客户端换维度清标记。

来源能力：8×8区块、一次一个异步FULL加载、每tick至多2个非空含矿section、连通矿簇、结构起点与结构引用、每包512标记、最近2048候选/512框/64标签、每30秒复查、可选Xaero结构点。去除敌人生灵全链。补齐来源缺失的结构实际画框/标签。所有主队列与结构引用额外chunk在异步加载前检查完整chunk位于worldborder内，避免越界生成。每玩家UUID作为ticket key，避免多玩家同块共享票据互相释放。客户端绘制恢复进入前GL状态。

新增GameTests三项：真实现有账号扣费/门槛/枯竭停止（停止发生于chunk工作前）、矿簇与五行全等级矿石、真实packet codec/512界限。未运行构建或实机视觉验收。须由根统一验证；不宣称光影兼容。

# 识海特效与天地为炉界面 — 2026-10-27

本次范围是开发进度快照中的两个界面及神识技能视觉。用户已批准四技能卡片与紧凑双栏参考图；沿用本对话“不使用 UI 工作台”的明确要求，直接提取、接入与实机复验。

## 设计及素材

- 原图、生成提示词、批准参考图及 12 项素材清单位于 `source-assets/sense-heavenly-ui-20261027-v1/`；`tools/extract_sense_heavenly_ui.py` 可重建正式贴图并输出裁切坐标、尺寸、SHA-256。
- 四种技能图标、翻页图标、外框、面板、普通按钮、选中/普通列表行、物品槽均来自批准图。清除参考图中烘焙的文字和物品，文字与数量由游戏实时绘制；裁掉外框角外背景。
- `SenseFurnaceArt.Button` 继承现有 `InkButton` 的点击、键盘与旁白逻辑，自绘普通、悬停、禁用、焦点状态。
- 识海采用两列四卡，窄页采用单列滚动；保留开辟入口、神识值/范围、锁定/探查状态、规则提示。小卡成本文字限制在按钮左侧，完整说明保留在悬停提示。
- 天地为炉采用 360×284 逻辑尺寸：左侧搜索/六配方/分页，右侧分类、成品、三项数值、材料与副药、开炉和返回。材料支持两排，三类副药仍通过背包槽位选择，不可复用同一槽。选择后优先保留年份，并由提示显示全名。

## 神识效果及边界

- 开辟：脚下双环及六道上升青色游丝，完成扩散；凝神辨识：目标聚焦环与读条收束；神识探查：随实际探查半径扩张的球面与地面波；锁定：金色目标环与脚下环；矿物/结构探查：紫青扩散与 64 区块进度环；移动/受击中断有短暂消散。
- 由成功的服务端动作产生瞬态视觉回执，沿用现有状态包；客户端请求、拒绝或重复施法不能凭空产生成功效果或重复扣费。回执不进入永久账户。
- 原有境界、消耗、冷却、凝神、辨识目标与遮挡解锁规则保持；沿用 D25 的统一神识账户及矿物/结构探查，不增加穿墙生灵高亮，不修改创世仙途或 JEI。
- 专用 GLSL 程序在世界渲染后合成，采样真实场景深度。保存并恢复 FBO、纹理、视口、混合、深度、裁剪与着色器状态；跳过光影阴影 pass。

## 可重现验收

```powershell
./gradlew.bat verifyFull --rerun-tasks --offline --no-daemon
./gradlew.bat runGameTestServer -PtalismansOnly --offline --no-daemon
./gradlew.bat runClient -PmyriadCapture -PsenseHeavenlyCapture -PtalismanUserStack -PmyriadCaptureOutput=reports/sense-heavenly-ui-release --offline --no-daemon
./gradlew.bat runClient -PmyriadCapture -PsenseHeavenlyCapture -PmyriadCaptureOutput=reports/sense-heavenly-ui-release-no-jei --offline --no-daemon
./gradlew.bat runClient -PmyriadCapture -PsenseEffectsCapture -PtalismanUserStack -PmyriadExpectedShaderPack=Bliss -PmyriadCaptureOutput=reports/sense-effects-bliss-final --offline --no-daemon
```

光影用隔离客户端配置启用实例所选 `Bliss_v2.1.2_(Chocapic13_Shaders_edit).zip`；无光影轮禁用该配置，不改用户实例配置。VFX fixture 使用真实服务端技能包与状态包；UI fixture 使用模拟目录/账户和真实背包副药，只证明布局及控件状态。两者证据不可混用。

全部客户端测试带独立禁止鼠标捕获保护，通过内部接口操作并正常保存退出，保护与测试代码不进入正式 JAR。实际测试结果与安装记录见下方补充。

## 本轮结果

- 完整 `verifyFull --rerun-tasks` 构建成功，354/354 服务端测试通过，其中 3 项新增神识回执测试已归入默认完整测试命名空间。
- JEI + Embeddium + Oculus 环境与不含 JEI 的环境分别通过 26 个 UI 状态；普通、悬停、禁用、焦点、搜索选区、无匹配、分页、炼器、三种真实副药槽、12 用材两行、凝神、锁定、低神识、窄页、冷却和探查状态已截图。
- 实际 3 倍 GUI 在 2400×1421 生效；配置 4 倍时窗口被系统限制为 2564×1421，现有自适应逻辑保持 3 倍。本轮不虚报实际 4 倍宿主界面验收。炉子另在配置 3/4 倍下验证其自身缩放。
- 无光影和启用 Bliss 各 10 个真实服务端技能场景通过，GL 错误均为 0；包含第一人称、凝神完成、移动中断、锁定遮挡和停止矿物探查。所有客户端鼠标捕获违规为 0。
- 外框四角、四边、标题接合和控件状态原生放大裁图位于同目录 `evidence/`，裁图保留外侧世界像素，没有用遮罩伪造背景裁剪。完整结果与哈希见 `evidence/verification.json`。
- 正式包不含 QA、GameTests 或禁止鼠标捕获保护；新贴图与 GLSL 在 JAR 内逐字节匹配。安装至约定“测试（1）”实例，SHA-256 为 `fd8223f6d82d9d69339119de36b8bc27bffab555d21c385cd1671e14e1a74ccb`。旧包由安装器备份，其他 64 个 Mod JAR 哈希不变。
- 用户原有 FlightNurture 测试和本地说明已恢复并与先前保护记录核对，不纳入本次提交。助手测试实例均正常保存退出；磁盘安装不代表已经运行的旧 JVM 加载新版。

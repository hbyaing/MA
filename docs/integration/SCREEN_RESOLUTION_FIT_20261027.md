# 全界面分辨率自适应（2026-10-27）

修复炼丹界面 420×320 逻辑尺寸在高 GUI 缩放下超出屏幕的问题，并统一覆盖本项目命名空间的全部 Screen（含以后新增界面）。不重做美术，不改变服务端菜单、槽位索引或玩法。

## 方案

- Forge ScreenEvent.Init.Pre 中先按当前真实 framebuffer、用户 GUI 设置、容器逻辑尺寸和外侧留白选择 GUI 密度，再更新 Screen.width/height，随后由原界面创建控件和槽位。
- 常规窗口尽量采用不超过用户设置的整数缩放；极小窗口使用小于 1 的比例保留完整逻辑画布。使用 Minecraft 自身的投影和鼠标坐标体系，不另加一套绘制/点击变换。
- 非容器界面至少保留 480×360 逻辑空间；容器按实际尺寸加 32px 留白，保留已有内部缩放。玄灵砧额外计算模板侧栏。
- 生存背包/阵法刻画保留既有密度策略；最小逻辑画布增加 480×326 兜底。
- 不写入 options.txt。切到其他界面的 Init.Pre 恢复用户 GUI 设置；关闭回世界在 ClientTick END 恢复。已有背包 removed 恢复逻辑保留。
- 适用于初始化、GUI 设置变化、窗口缩放和分辨率变化；炼丹与储物袋搜索文字在重新初始化时保持。
- 1920×1080 配置 GUI 4 时，420×320 炼丹窗口改用 GUI 3；1280×720 使用 GUI 2。JEI 本体不改动，仍按原生窗口尺寸决定可用侧栏。

## 已审计入口

|界面类|适配方式|
|---|---|
|AlchemyScreen.java|初始化前按容器尺寸选择 GUI 密度|
|ArtRadialScreen.java|初始化前预留 480×360 逻辑画布|
|ArtWheelScreen.java|初始化前预留 480×360 逻辑画布|
|BossTransformSkillScreen.java|初始化前预留 480×360 逻辑画布|
|CreatureCompassScreen.java|初始化前预留 480×360 逻辑画布|
|CultivationMainScreen.java|保留现有背包密度策略，并增加超小窗口兜底|
|EquipmentWorkstationScreen.java|初始化前按容器尺寸选择 GUI 密度；保留现有内部缩放与输入换算|
|HeartMethodScreen.java|初始化前预留 480×360 逻辑画布|
|HeavenlyFurnaceScreen.java|初始化前预留 480×360 逻辑画布；保留现有内部缩放与输入换算|
|HerbProcessingTableScreen.java|初始化前按容器尺寸选择 GUI 密度|
|HumanEmperorBannerScreen.java|初始化前按容器尺寸选择 GUI 密度|
|InnerVisionScreen.java|初始化前预留 480×360 逻辑画布|
|InscriptionScreen.java|初始化前按容器尺寸选择 GUI 密度；保留现有内部缩放与输入换算|
|RefiningForgeScreen.java|初始化前预留 480×360 逻辑画布；保留现有内部缩放与输入换算|
|ReforgeScreen.java|初始化前按容器尺寸选择 GUI 密度；保留现有内部缩放与输入换算|
|RobeScreen.java|初始化前按容器尺寸选择 GUI 密度|
|SoulFurnaceScreen.java|初始化前按容器尺寸选择 GUI 密度|
|SoulHealingRoomScreen.java|初始化前按容器尺寸选择 GUI 密度|
|SoulTradeScreen.java|初始化前按容器尺寸选择 GUI 密度|
|TalismanPlatformScreen.java|初始化前按容器尺寸选择 GUI 密度；保留现有内部缩放与输入换算|
|TransformSelectionScreen.java|初始化前预留 480×360 逻辑画布|
|XuanlingAnvilScreen.java|初始化前按容器尺寸选择 GUI 密度；额外预留 126px 模板侧栏|
|CarvingScreen.java|保留现有背包密度策略，并增加超小窗口兜底|
|QteScreen.java|初始化前预留 480×360 逻辑画布|
|BagScreen.java|初始化前按容器尺寸选择 GUI 密度|

## 验证与边界

- Gradle build 成功，现有静态检查和回归通过。
- 生产 ScreenFitPolicy 参与 3696 组分辨率/面板/配置/Unicode 边界测试，验证逻辑窗口可容纳且不超过用户配置；覆盖 320×240、640×360、854×480、1024×768、1280×720、1366×768、1719×1073、1920×1080、2560×1440、3440×1440、3840×2160。
- 此矩阵是尺寸计算回归，不是游戏截图或真实鼠标交互验证。按用户此前自行实测的安排，本轮不启动游戏客户端；全部界面控件状态、真实拖拽/焦点、JEI 共存及高 DPI 多显示器仍需实机验收，不能宣称这些已经通过。
- 本轮只改客户端适配，不重复运行未改变的服务端玩法测试。

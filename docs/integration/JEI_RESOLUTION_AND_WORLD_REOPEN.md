# JEI避让、原版标签与本机存档重进处理（2026-10-21）

用户要求1920×1080下像参考图完整显示背包三栏，重点是JEI不与生存背包重叠；随后要求创造分类选择标签恢复原版，并排查新世界退出后不能重进。

## 源码改动

1. CultivationMainScreen原getGuiLeft/Top/XSize/YSize只报告中间物品区，漏掉左侧导航与右侧概览。现在对外报告完整窗口；实际槽位仍使用独立leftPos/topPos与imageWidth/imageHeight，不移动到装饰边框坐标。
2. InventoryResolutionFit仅在自定义生存背包初始化/窗口重排时临时选择合适GUI密度，用户GUI设置不写入；关闭/换界面时重新按当前设置恢复。1920×1080、原GUI4时转为GUI2：逻辑画布960×540，窗口480×294，物理窗口960×588，两侧各留480像素，修炼概览完整显示。2560×1440使用GUI3；1280×720允许GUI2。用户主动选择更小GUI缩放时保留；不修改HUD配置。
3. 已读取本机JEI 15.71.0.224的Forge ScreenHelper字节码，确认其通过这四个公开getter获取边界；沿用Forge边界接口，不引入新的JEI必需依赖。
4. ModTabs取消withTabsImage覆盖，分类选择标签及原生滚动柄使用Minecraft默认图集；十页内部保留宣纸水墨背景、物品格和墨色标题。

## 本机重进问题

当前实例latest.log与2026-10-21-1.log.gz多次出现WorldOpenFlows Failed to read level与DirectoryLock IOException（Windows文件锁定）。错误栈经过Forge实验性数据包确认页与Citadel 2.6.3 ClientProxy.onOpenGui。已检查该Citadel类的字节码：skipWarnings启用时会在Opening事件内直接执行确认回调，与本次重入调用链一致；这是本轮定位到的触发路径，尚未实机复测。

本机config/citadel-common.toml的"Skip Datapack Warnings"由true改为false，保留正常确认流程。原配置已备份到实例mod-backups/world-reopen-20261021-051508（含哈希核对），配置最终采用原子替换以避免运行中监听器读到部分内容。没有删除session.lock，没有改动level.dat或世界数据。

请完整退出并重启游戏，释放当前失败尝试保留的锁；重进世界时正常处理实验性设置确认。若仍失败，需以重启后的最新错误继续诊断，不能将本次配置处理宣称为已完成重进实测。此项是当前实例配置修正，不嵌入或分发Citadel代码，也不替换其他Mod。

## 本轮验证

遵守用户此前直接打包、自行测试的要求：仅运行jar/reobfJar构建，36秒成功；未启动Minecraft、GameTests或GUI测试。此前339项测试与截图不作为本轮实机证据。成品资源/类排除检查见ui-jei-native-tabs-package-audit-20261021.json。

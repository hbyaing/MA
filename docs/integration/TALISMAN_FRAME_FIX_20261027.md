# 绘制台、镌印台标题与背景裁剪

2026-10-27。在同步 main 的 39a1a30 后修复用户截图所示的两个问题，保留 430×340 布局、全部槽位坐标与 3×9 背包加 1×9 快捷栏。

- 标题区域移除代码矩形遮罩，并统一整条标题栏的墨色，消除原素材烘焙的标题底块；保留原有两侧金线装饰与连续顶框。
- 背景通过原图坐标的八边形轮廓绘制，保留原 UV 和内容位置，裁去四边及斜角外的留边；同时取消界面背后的矩形世界暗化遮罩。
- 复用现有素材和控件，不修改 JEI 本体。新增客户端验收入口显式启用，鼠标保护及测试夹具不进入正式包。

## 验证

- `./gradlew.bat verifyFull --rerun-tasks --offline --no-daemon`：28 项任务执行，351 项必需 GameTests 全部通过。最终标题栏绘制调整后再次执行正式 build，通过资源校验与打包。
- `./gradlew.bat runClient -PmyriadCapture -PtalismanCapture -PmyriadCaptureOutput=<绝对输出目录> --offline --no-daemon`：无 JEI 的 14 个实机状态通过；加 `-PjeiCapture` 后再次通过相同 14 个状态。
- 两个界面分别覆盖普通、悬停、禁用、焦点、搜索输入及 GUI 缩放 2/3/4。标题、四角、四边放大逐项复核；鼠标捕获违规均为 0。
- 完整服务端测试使用仓库原有 FlightNurtureGameTests；原有本地专用测试修改已独立备份，未加入本次提交。
- 正式 JAR 扫描未包含 QA、GameTests 或鼠标保护类，安装脚本已核对目标实例哈希。磁盘安装不代表运行中的 JVM 已加载更新。

正式包 SHA-256：`3dd9850b7a36ef5863ac7b5bba9e9041135c93eef5823966d46c4dca73eea001`。

[两窗实机预览](evidence/talisman-frame-20261027/preview.png) · [标题及外框逐项裁图](evidence/talisman-frame-20261027/edges-and-titles.png) · [无 JEI 结果](evidence/talisman-frame-20261027/no-jei.json) · [有 JEI 结果](evidence/talisman-frame-20261027/jei.json)

以上为本次局部界面验收，项目仍是开发进度快照。

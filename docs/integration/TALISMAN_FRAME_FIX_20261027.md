# 绘制台、镌印台标题与背景裁剪

2026-10-27。在同步 main 的 39a1a30 后修复用户截图所示的两个问题，保留 430×340 布局、全部槽位坐标与 3×9 背包加 1×9 快捷栏。

- 标题区域移除代码矩形遮罩，并统一整条标题栏的墨色，消除原素材烘焙的标题底块；保留原有两侧金线装饰与连续顶框。
- 背景通过原图坐标的八边形轮廓绘制，保留原 UV 和内容位置，裁去四边及斜角外的留边；同时取消界面背后的矩形世界暗化遮罩。
- 复用现有素材和控件，不修改 JEI 本体。新增客户端验收入口显式启用，鼠标保护及测试夹具不进入正式包。

## 验证

- `./gradlew.bat verifyFull --rerun-tasks --offline --no-daemon`：28 项任务执行，351 项必需 GameTests 全部通过。最终标题栏绘制调整后再次执行正式 build，通过资源校验与打包。
- `./gradlew.bat runClient -PmyriadCapture -PtalismanCapture -PmyriadCaptureOutput=<绝对输出目录> --offline --no-daemon`：无 JEI 的 16 个实机状态通过；加 `-PjeiCapture` 后再次通过相同 16 个状态。
- 两个界面分别覆盖普通、悬停、禁用、焦点、搜索输入及 GUI 缩放 2/3/4。标题、四角、四边放大逐项复核；鼠标捕获违规均为 0。
- 完整服务端测试使用仓库原有 FlightNurtureGameTests；原有本地专用测试修改已独立备份，未加入本次提交。
- 正式 JAR 扫描未包含 QA、GameTests 或鼠标保护类，安装脚本已核对目标实例哈希。磁盘安装不代表运行中的 JVM 已加载更新。

正式包 SHA-256：`3dd9850b7a36ef5863ac7b5bba9e9041135c93eef5823966d46c4dca73eea001`。

[两窗实机预览](evidence/talisman-frame-20261027/preview.png) · [标题及外框逐项裁图](evidence/talisman-frame-20261027/edges-and-titles.png) · [无 JEI 结果](evidence/talisman-frame-20261027/no-jei.json) · [有 JEI 结果](evidence/talisman-frame-20261027/jei.json)

以上为本次局部界面验收，项目仍是开发进度快照。

## 框外留边复查

补充白色/粉色棋盘底的独立实机验收，消除暗色游戏场景对边界判断的干扰。两窗在有 JEI、无 JEI 下，分别检查框外 24,266 个像素（外框抗锯齿保留 2 屏幕像素容差），均未改变棋盘背景像素。原双窗预览误将框外游戏画面裁进了拼图；现仅对评审预览按外框轮廓加透明底，正式 UI 仍使用真实裁剪绘制，不依赖预览遮罩。

[绘制台高对比实机原图](evidence/talisman-frame-20261027/draw-bright-edge.png) · [镌印台高对比实机原图](evidence/talisman-frame-20261027/engrave-bright-edge.png) · [框外像素验证](evidence/talisman-frame-20261027/edge-pixel-result.json)

后续实机反馈及正式源贴图透明化修复见 [框外像素透明化](TALISMAN_ALPHA_CROP_20261027.md)。前次仅凭预览留边和旧进程作出的解释不作为实机问题已解决的依据。

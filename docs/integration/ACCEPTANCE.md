# 本轮验收清单 · 老UI路线

基线、依赖和协议见 acceptance-baseline.json。此清单不改变D01–D30的已确认玩法取舍。

## 服务端（本轮范围）
- 完整运行verifyFull，不加myriadExternalShaders、myriadCapture或UI专项筛选。
- 日志出现GAME TESTS COMPLETE、零必需失败，并核对实际测试数量和测试结果报告。
- D28会话/时序/防重放/突破门槛/奖励与保存；D29致命伤/冷却/特殊死亡排除；D30真实菜单/药龄/费用/取消退款/失败消耗/冰火毒附击。
- 储物袋、四级货币、材料托管、主人继承、剑阵生命周期回归。
- 正式jar不得包含QA测试类、鼠标捕获保护或外部光影依赖。

## 后续独立验收（不随服务端通过自动打勾）
- 原生客户端老UI与D28–D30、罗盘、阵法模型。
- Complementary光影与透明排序/遮挡。
- 自然生成、保存重载、旧档/多人。

## 执行

Java17，Gradle8.8，Python3及Pillow>=10,<12：
```
./gradlew verifyFull -PpythonExecutable=/path/to/python --console=plain
```
Windows使用gradlew.bat。第一次解析依赖需要联网；准备完依赖后可加--offline。
服务端禁止携带myriadExternalShaders，该标志仅供单独runClient使用。

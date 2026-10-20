# 炼丹交互工作台

按项目约定，先启动统一入口：

`C:\Users\33140\Documents\Codex\Workbenches\启动全部工作台.py`

从该入口点击“万界仙途 · 炼丹工作台”。本地安装源为本目录；运行 `python tools/install_alchemy_workbench.py` 更新入口中的副本。脚本复用游戏正式面板、按钮、槽位与外框素材，不修改原始贴图。统一启动器的恢复副本在 `tools/workbench-hub/`。

预览支持四阶段、每阶段一次控火、独立长按注灵，以及普通、悬停、禁用、焦点状态和三种输出缩放。下方放大部件用于对照边框和布局。浏览器文字为近似显示；实机验收使用 `-PalchemyReservationCapture runClient`，截取正式 `AlchemyScreen` 并检查原生文字。该测试源集的 Mixin 在方法入口阻止鼠标捕获，保护代码不进入正式 JAR。

浏览器预览不连接游戏，不修改存档。数值、成丹率与实际操作结果以游戏服务器为准。

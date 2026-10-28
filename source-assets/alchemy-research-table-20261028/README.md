# 丹道研析台 · 已批准V2与游戏界面

用户2026-10-28提供图片并明确要求按图制作UI，批准参考保存在 approved-reference.png。concept-v1/v2为原始方案，保留追溯；本次已注册实体工作方块与菜单并接入真实基础观察/记录，完整09–11研究功能尚未完成。

生产底图：src/main/resources/assets/wuxingxiantu/textures/gui/research_v2/background.png，使用内置image_gen按批准参考清除文字和示例物品。渲染时按 ui-elements.json 选区复用，背包按真实菜单重建为9×4；不直接沿用生成图格子。皮肤复用InkButton行为，文字与物品均为实时绘制。

统一工作台：python docs/previews/启动全部工作台.py --open 炼丹重塑 --no-browser，然后打开 research.html。页面在原alchemy-rework工程内，未另建浏览器项目。

实机对照及边角/纸张/背包裁图见 docs/integration/evidence/research-table-ui-20261028；能力与验证范围见 docs/testing/RESEARCH_TABLE_UI_20261028.md。图片与生成提示规格只说明视觉来源，不冒充反推/认证算法。

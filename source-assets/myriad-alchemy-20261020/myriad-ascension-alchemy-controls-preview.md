# 炼丹控火布局预览

本目录的 myriad-ascension-alchemy-controls-preview.png 是基于仓库既有炼丹 UI 概念稿编辑的设计预览，不是实机截图或已实现功能证明。

保留三栏布局、八卦八槽、原生背包、核心升阶、长按注灵、异火与药力信息。新增四阶段条与独立控火入口。生成图片中的文字、卦字、物品数量和按钮状态仅示意；实装须使用正式控件与原生字体，在普通、悬停、禁用、键盘焦点和不同 GUI 缩放下复验。

按 AGENTS.md 的界面流程，当前等待用户批准该布局，再进入工作台与实装。约定 E:\着色器渲染\启动全部工作台.py 在当前主机不存在；已询问新的统一入口路径，未绕过约定另行启动工作台。

使用内置 image_gen。完整提示：

Use case: ui-mockup / precise image edit. This image is the existing approved design reference for a Minecraft xianxia alchemy screen, NOT a live screenshot. Produce one polished updated design concept that adds manual fire control while preserving the existing three columns, dark teal and gold ink landscape theme, all ornamental frame corners, eight bagua ingredient slots around the central pill preview, recipe list at left, probability and fire affinity at right, bottom vanilla item inventory and furnace core slot. Do not redesign the overall screen. Exact title at top: 仙阶炼丹炉. In the existing long narrow progress band immediately above the bottom inventory, replace the empty bar with four legible connected stages labeled exactly 温炉 → 淬药 → 融药 → 凝丹, with 淬药 highlighted in warm gold and the other labels subdued. Under 八卦炼丹 at center, add a small subtitle 主药 · 稳炉 · 药性 · 增产; retain the original 卦字 in all eight slots. Keep the separate 注灵 panel and its 长按注灵 button unchanged. Within the bottom-right action panel, replace the redundant 材料不足 status row above the large 开始炼丹 button with a compact 火候 meter, a visibly marked gold ideal window and a matching dark-gold 控火 button to the right. The big 开始炼丹 button remains below, clearly distinct from 控火. All buttons use the existing ornate ink/gold style, never vanilla gray. Align baselines and inner padding carefully; no overlap, no added outer frame, no duplicated buttons. Preserve enough room for all original existing controls. This is a visual design preview; do not add explanatory text outside the game UI.

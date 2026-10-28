# Image generation prompts

Tool: built-in image_gen. Mode: generate V1, edit V1 into V2. Images are proposed concept art, not a shipped runtime UI.

## V1 prompt specification

Create an original high-quality game development concept sheet for a Minecraft 1.20.1 Chinese cultivation mod called 万界仙途. Proposed new workstation 丹道研析台, not a furnace or an actual screenshot. Dark blue-green ink panels, antique gold double borders, parchment writing area, distant ink mountains, jade highlights. No vanilla gray buttons, modern dashboard, purple sci-fi holograms or steampunk. Left27%: one-block-footprint dark carved wood voxel research desk, bronze specimen bowl, inkstone, open notebook, specimen bottle, small item icon. Right70%: frontal readable UI titled 丹道研析台, six tabs 鉴定/实验/反推/丹方/谱系/认证, 鉴定 selected. Three panels: 研究样本, 鉴定记录, 研究线索. Golden pill specimen and waste/ash/residue icons; paper and ink consumables. Illustrative observations 恢复气血/温润/尚未辨明/样本观测. Known and hypothetical condition cards with explicit uncertainty and no original recipe or seed disclosure. Contextual 开始鉴定/记录结果 actions and 9x4 inventory. Caption 外观与界面初稿; footer 设计参考 · 非游戏实机.

## V2 edit prompt specification

Preserve the voxel desk, dark wood, bronze bowl, notebook, inkstone, bottle, small icon, mountain header, teal/gold palette and six tabs. Correct inventory to exactly nine columns and four rows,36 slots, hotbar separated by a small gap. Use ink-teal slots and antique gold rims. Compact main panels/header to fit inventory without overlap or clipping. Only one framed specimen slot; waste/ash/residue become an unframed 可鉴定 legend; keep two paper/ink consumable slots. 已知条件: 药效：恢复气血, 痕迹：温润. 待证假设: 年份范围未知, 需补对照实验. Discreet 不显示原始配方. Center observations unchanged. Keep two contextual custom buttons above inventory. Left description: 专用于丹药鉴定与研究;记录实验，反推候选丹方;查看谱系，提交独立复验. No automatic original-recipe recovery. Caption 外观与界面优化方案 V2; keep non-runtime footer. No extra panels, percentage chances, hidden recipe lists or dashboard charts.

上述为可读提示规格；具体采纳前仍按项目UI审批流程，不能据此视为素材切片或模型已经交付。

## 批准后生产底图（image_gen edit）

输入用户批准参考，输出仅右侧游戏UI，约1.467:1。保留四角、双线外框、山水页眉/下景、宣纸标题与正文、六页按钮、三栏、单样本及纸墨槽、两枚动作按钮、背包外框。移除全部文字、物品图、示例数据和左侧展示板。全部动态字/物品由游戏绘制。生成底图中的背包格子数量未直接采纳，最终以真实菜单坐标绘制36个独立格子，并按UV重建边框。

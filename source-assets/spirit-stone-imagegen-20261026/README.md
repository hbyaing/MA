# 灵石贴图生图记录（2026-10-26）

使用内置 image_gen 工具，透明背景。generated.png 为原始生成结果；before.png 为替换前贴图；preview-32.png 为正式 32×32 贴图的最近邻放大预览。

提示词：单枚修仙灵石物品贴图，透明背景，左下至右上倾斜的厚实不规则六棱晶体；蓝紫、淡薰衣草色切面，青色内部灵气脉络、白色高光、深靛蓝暗部；像素风、大切面、清晰轮廓；无底座、文字、界面、背景及游离粒子。

原始英文提示词约束：Use case: stylized-concept. Production-ready Minecraft xianxia spirit stone ITEM TEXTURE. Single isolated crystal on truly transparent background. Square canvas. Chunky irregular elongated hexagonal faceted spirit crystal, tilted diagonally from lower left to upper right, occupying 80 percent of canvas. Violet-blue and pale lavender crystal with cyan inner spiritual veins and bright white facet highlights, deep indigo shaded edges. Crisp pixel-art aesthetic at apparent 32x32 sprite grid, large readable facets, limited palette, nearest-neighbor hard pixel edges. Readable silhouette in inventory at 16px. Three-quarter view, thick solid crystalline volume, modest inner light contained within crystal. No pedestal, ground, external shadow, detached sparkles, text, border, UI or other objects.

正式资源：src/main/resources/assets/wuxingxiantu/textures/item/spirit_stone.png。由生成原图最近邻缩至 32×32，保留透明通道。既有四档物品继续使用其已有模型继承关系，本次只替换共用贴图。

验证范围：透明通道、尺寸、缩小后轮廓与切面检查、正式 JAR 构建及包内资源一致性。未进行客户端实机验收；本记录不代表整体项目验收完成。

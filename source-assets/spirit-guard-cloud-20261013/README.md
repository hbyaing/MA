# 云篆护纹原创遮罩

生成器：`tools/generate_guard_cloud_mask.py`。`cloud-brush-source.json` 保存原创三次贝塞尔中心曲线、沿程宽度、进度范围和精确原版装备 UV 布局。

`cloud-brush-outlines.svg` 是按上述曲线与宽度采样的闭合笔形轮廓，尖笔尾由宽度渐零产生，不使用随机噪声、规则网格或平铺。主簇三笔，副簇一到两笔，各簇 phase 错开。

数据图为 1024×512 RGBA：R=A=coverage，G=pathProgress×coverage，B=clusterPhase×coverage；空白像素四通道全零。收窄到0.32像素的抗锯齿边缘，避免小笔画被柔边吞没。着色器采样后用 g/r 与 b/r 恢复进度和簇 phase，线性过滤不把边缘混为零 phase。数据图不能作为普通彩色贴图展示。

`cloud-brush-color-preview.png` 只便于观察笔形；`rgba-contract-preview.png` 为通道伪彩色。`netherite-cloud-comparison.png` 使用玩家本地原版下界合金纹理进行静态对照，非游戏截图、非最终动态效果，也不包含在 mod 资源中。底材从未被修改。

资源路径：`assets/wuxingxiantu/textures/effect/spirit_guard_cloud_mask.png`。装备复用原版 64×32 UV，动画循环由着色器控制，支持甲面逐笔显现、沿笔形亮芯以及接力消散。

该原始资源现作为灵力御体III，像素保持不变。I、II、IV新增独立遮罩，完整分级配方、统计与静态资产对照见[四级说明](levels/README.md)。

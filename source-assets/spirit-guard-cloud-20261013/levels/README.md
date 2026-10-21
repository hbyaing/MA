# 灵力御体四级云篆资产

生成器：`tools/generate_guard_cloud_mask.py`。三级沿用原始资源，其余三级写入新文件。`cloud-brush-levels.json` 保存全部原创贝塞尔中心曲线、宽度、逐笔进度、簇 phase 与各级装备 UV 布局。

**I 初护：**完整双笔主云簇与腰部、正背侧基础护纹，共16笔；以柔和湖蓝和较慢流光表现初阶。

**II 成纹：**沿一级主纹补齐5处侧面细笔，共21笔；以清透青蓝与明亮流光区分，纹路密度平滑递进。

**III 云篆：**原版完整三笔主簇与侧面护纹，像素与现有三级资源完全相同。

**IV 护篆：**在三级原纹上仅添八处短护笔与细分枝，围绕既有主纹留白，不平铺、不加柔光。

| 等级 | 造型 | 云簇数 | 笔数 | 全图实际覆盖 | 游戏资源 |
|---|---|---:|---:|---:|---|
| 1 | 初护 | 12 | 16 | 1.221% | spirit_guard_cloud_1.png |
| 2 | 成纹 | 17 | 21 | 1.351% | spirit_guard_cloud_2.png |
| 3 | 云篆 | 17 | 25 | 1.453% | spirit_guard_cloud_mask.png |
| 4 | 护篆 | 25 | 33 | 1.717% | spirit_guard_cloud_4.png |

所有等级均为1024×512，使用原版64×32装备UV与0.32像素窄抗锯齿边缘。R=A=coverage，G=pathProgress×coverage，B=clusterPhase×coverage；空白RGBA全零。颜色、亮度、流速和受击强度由游戏接入控制，等级遮罩只决定独立笔形层次，不改原装备底材。三级像素不变，四级在原有云纹之外添加少量细笔。

`level-comparison.png`、`level-N-brush-preview.png` 和 `level-N-outlines.svg` 只作静态原创资产说明，不代表游戏实测、最终动态效果或后处理发光。数据PNG不能当普通彩色贴图显示。

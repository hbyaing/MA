# 彭矫·足欲之邪

地仙期突破试炼，下尸·居人足。三尸系列最后一位 Boss 的可编辑美术源文件。

## 设计

整体约 8.2 格高，以白炽魂核、五色烟光和螺旋妖焰构成悬浮主体。红、绿、蓝、紫、黄五个魇鬼是同源妖焰凝成的分身：傩面式鬼脸、空洞斜眼、獠牙、额上符纹、佝偻肩背、空腔胸口与长钩鬼爪。长袖和交领残片保留中式邪祟意象，下身化成三条蛇形魂须，不再具有凡人的头发、肤色、双腿和完整衣袍。

主体下方十五条彩色蛇形卷须接近地面后卷曲外张，每条含四段独立骨骼。分身、主体光带和贴地卷须共用五色半透明质感，亮色魂丝贯穿分身与光壳，弱化规则玻璃方块和机械光环的观感。与前序 Boss 的区别在于更大的整体轮廓、五方合围的分身和大范围贴地卷须。保持仅制作 Boss 的范围，没有场景。

## 文件

- `pengjiao.bbmodel`：可编辑工程，包含贴图、骨骼、八组动画。
- `pengjiao_atlas.png`：256×256 贴图，含透明色块与亮色部位。
- `pengjiao_preview.png`、`pengjiao_front.png`、`pengjiao_back.png`：静态预览。
- `pengjiao_orbit.png`、`pengjiao_assault.png`、`pengjiao_convergence.png`：动作采样预览。
- `bedrock_source/pengjiao.geo.json`、`pengjiao.animation.json`：Bedrock 几何和动画源文件。
- `bedrock_source/particles/`：五种幻光和一组五色地面余辉定义。
- `bedrock_source/textures/particle/`：星芒与余辉贴图。
- `validation.json`：几何、骨骼引用和动画采样检查记录。

## 动画

所有标识使用 `animation.pengjiao.` 前缀。

| 标识后缀 | 时长 | 视觉行为 |
| --- | --- | --- |
| `prismatic_core` | 8 秒，循环 | 光核变形、五色外壳与破碎光环旋转 |
| `fivefold_orbit` | 18 秒，循环 | 五分身沿光核周围旋转，反向抵消自身倾斜以保持直立 |
| `restless_desire` | 4 秒，循环 | 蛇形卷须游动、五色碎光闪烁 |
| `split_manifest` | 4 秒 | 五个人形由光核中分离显形 |
| `fivefold_assault` | 2.4 秒 | 五分身错相前冲、挥臂，卷须扫击 |
| `mirage_convergence` | 4 秒 | 分身回收，光核膨胀后重新释放 |
| `severed_illusion` | 4.5 秒 | 分身、光壳、光环与卷须收缩消散 |
| `ghost_shroud_drift` | 4 秒，循环 | 魇鬼残袖、灵体下摆与蛇形魂须轻摆 |

已检查环绕四分之一周期时幻影仍直立，显形起点缩放为 0.03，聚合期间内核保持 1.65 倍。运行时应由动画控制器切换技能；聚合、分裂、死亡等状态需要暂停环绕并协调混合，避免同时驱动同一骨骼产生不期望的叠加。

## 粒子

| 幻影骨骼 | 粒子标识 |
| --- | --- |
| `crimson_apparition` | `cultivation:pengjiao_crimson_light` |
| `jade_apparition` | `cultivation:pengjiao_jade_light` |
| `azure_apparition` | `cultivation:pengjiao_azure_light` |
| `violet_apparition` | `cultivation:pengjiao_violet_light` |
| `gold_apparition` | `cultivation:pengjiao_gold_light` |

在上述骨骼附近添加粒子定位器并绑定发射器，产生对应颜色的飘散星光。`cultivation:pengjiao_five_color_trail` 放在实体脚下，随机产生五色淡出余辉。默认世界空间运动让粒子离开移动中的主体后保留短暂残影。

## 接入说明

本包是美术源文件，不是可直接安装的完整模组或资源包。需要目标项目补齐实体定义、透明和自发光材质、动画控制器、粒子定位器及技能行为。五个分身当前属于同一模型的独立骨骼，独立寻路、独立受击或各自血量需要游戏逻辑支持。当前模型不包含真实体积光和环境照明，幻光效果依赖目标渲染方案。

已检查模型导出、骨骼动画引用、JSON 与粒子贴图路径，未在 Minecraft 实机验证。Java 版需按目标加载器或插件转换。魇鬼版本有 2873 个立方体、140 个骨骼。建议在目标平台评估透明材质叠加和同屏性能。为保持现有动作兼容，部分内部骨骼名仍沿用 `flowing_dao_robe` 和 `silk_ribbon`；对应几何已替换为魇鬼魂须和残袖。

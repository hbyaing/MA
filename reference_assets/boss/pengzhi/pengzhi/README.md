# 彭质·腹欲之魔

大乘期突破试炼，中尸·居人腹。第二阶三尸系列 Boss 美术源文件。

## 外观

约 6.4 格高的肥胖畸形实体，暗紫裂肤、隆起肩背、沉重双臂与短粗双足。纵向腹裂保留内壁与深腔，十条粉紫色触手从腔内伸出；每条触手有五段独立骨骼和一张带牙、流涎的末端口器。头部、手心与后背另有五张嘴，全身共十五张口器。骨刺和泛黄牙齿衬托暗紫皮肤，触手亮色脉点强化内脏活性。

沿用仅制作 Boss 的范围，本包没有生成酒池肉林场景。

## 文件

- `pengzhi.bbmodel`：可编辑的 Blockbench 工程，含内嵌贴图、骨骼和动画。
- `pengzhi_atlas.png`：256×256 像素贴图。
- `pengzhi_preview.png`、`pengzhi_front.png`、`pengzhi_back.png`、`pengzhi_slam.png`：模型与挥砸动作预览。
- `bedrock_source/pengzhi.geo.json`：Bedrock 实体几何源文件。
- `bedrock_source/pengzhi.animation.json`：六组动画。
- `bedrock_source/particles/`、`bedrock_source/textures/particle/`：紫液与蒸发毒雾的粒子定义和贴图。
- `validation.json`：导出与动作采样检查记录。

## 动画

| 动画标识后缀 | 时长 | 用途 |
| --- | --- | --- |
| `heavy_breath` | 4 秒，循环 | 沉重呼吸与肩臂起伏 |
| `tentacle_hunger` | 4 秒，循环 | 十条触手错相蠕动 |
| `maws_and_drool` | 2 秒，循环 | 口器咀嚼与几何液滴下落 |
| `devour` | 2.6 秒 | 触手收拢、腹部吞食膨胀 |
| `tentacle_slam` | 1.8 秒 | 蓄力后前挥砸击 |
| `wine_frenzy` | 3 秒 | 汲取力量、躯干膨胀和双臂外张 |

完整标识统一使用 `animation.pengzhi.` 前缀。动作已在 Blockbench 采样检查；实际动画混合、攻击碰撞与伤害帧需要由游戏端控制。

## 粒子接入

将 `cultivation:pengzhi_venom_drip` 发射器绑定到各末端口器下缘，并使位置随对应 `tentacle_N_terminal_maw` 骨骼移动。每口默认每秒两滴。液滴使用世界空间运动，重力下落，碰撞时终止并触发 `cultivation:pengzhi_venom_mist`。雾团短时扩大、上浮并淡出。默认碰撞包括墙面；如需严格只在地面蒸发，应由游戏端过滤碰撞并触发雾效。

模型中的流涎几何用于 Blockbench 预览；游戏端持续滴落应使用粒子，可移除或隐藏 `_drop_` 骨骼避免两套液滴同时显示。

## 接入边界

本包是美术源文件，不是可直接安装的模组或完整资源包。需要补齐实体定义、材质、动画控制器、粒子定位器和技能逻辑。当前亮色贴图表示发光组织，真实自发光材质与环境照明需在目标渲染方案中配置。粒子 JSON、引用与贴图已检查，尚未在 Minecraft 实机运行。

食台回血、酒池狂暴、地面减速 30%、毒雾伤害属于游戏逻辑，本包仅提供相关视觉动作，不实现这些机制。Java 版接入需要目标加载器或插件支持的模型转换。

模型含 1568 个立方体、119 个骨骼。建议按目标平台进行性能测试，并在需要同屏大量实体时另做简化版本。

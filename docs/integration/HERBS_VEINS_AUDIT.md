# 灵脉与草药核对（第5项，2026-10-21）

以五行灵植/世界生成管理为权威，18来源草药保留独立物种。以下清单由当前ImportedHerbs与实际配置资源核对生成；静态资源完整不能替代自然世界采样。

| ID | 名称 | 药阶潜力上限 | 生成规则 | 生境 |
|---|---|---|---|---|
| herb_juling | 聚灵草 | 3 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_huayuan | 化元草 | 4 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_xisui | 洗髓草 | 4 | SURFACE_NEAR_WATER | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_pozhang | 破障草 | 5 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_huichun | 回春草 | 3 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_xingchen | 星辰草 | 5 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_tianji | 天机花 | 5 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_huanling | 幻灵草 | 4 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_fengling | 丰灵草 | 3 | SURFACE_NEAR_WATER | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_hanbing | 寒冰草 | 4 | SURFACE_HIGHLAND | ['minecraft:snowy_slopes', 'minecraft:frozen_peaks', 'minecraft:jagged_peaks'] |
| herb_lieyang | 烈阳花 | 4 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_jingang | 金刚草 | 4 | SURFACE_ROCK_EDGE | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_duzhen | 毒鸩草 | 4 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_jifeng | 风息草 | 4 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_lingzhi | 百年灵芝 | 4 | FOREST_FLOOR | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_leiteng | 雷藤 | 4 | SURFACE_HIGHLAND | ['minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:jagged_peaks'] |
| herb_yanjia | 岩甲花 | 4 | SURFACE_ROCK_EDGE | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |
| herb_lingdao | 灵稻 | 3 | SURFACE_NEAR_WATER | ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:flower_forest'] |

18项均有已匹配的configured_feature、placed_feature、biome_modifier、loot_table和中文名称。五行疾风草gale_grass与来源风息草herb_jifeng各自保留，不因药性近似合并。数据见herb-resource-audit-20261021.json。

## 药龄与培养

- 外观成熟与药龄独立：500年起一阶，之后每2000年一阶；上限由物种潜力、灵壤及灵气场共同约束。
- 背包不增长药龄；种植保留主株药龄与丹灰次数；额外繁殖掉落从零药龄起步。
- 实际累计由随机刻观测推进，每次最多累计600tick；这限制离线补时，但不等同于逐tick连续在线计时的实机验收。
- 既有测试覆盖18物种注册、炮制保龄、门槛和采收重种；本轮新增成熟多掉落仅一株继承年份、同tick重种不被延迟清理擦除、丹灰三次上限保存。

## 灵脉与旧地形保护

原算法在候选区块周围15格扫描连续洞壁。仅候选来自新区块并不足以保证邻居也是新区块，旧实现可能在边界替换旧地形。

本轮在SpiritVeinFeature真实地物装饰入口标记本区块，SpiritVeinData保存DecorationChunks；生成阶段只允许扫描/写入已标记区块。标记早于种子是否有脉的判断，因此无脉的新邻居也能参与连续矿带。延迟队列仍等待九宫格已加载区块，不主动加载地形。

兼容规则：旧存档缺少DecorationChunks时，不从Pending或已有矿脉记录推断写权限；已成功生成的矿脉/探测/灵气记录不变。旧版遗留待生成候选因资格不明跳过并结束该候选，不补写旧地形。边界可用洞壁不足时允许不成脉，优先保证既有存档地形。

本轮回归使用生产generate算法和受控跨区块洞壁，检查实际写入坐标全部位于获准区块；另测资格NBT保存及旧版队列不能扩大权限。此测试是受控地形，不宣称正常世界生成分布已验收。

罗盘读取当前维度实际SpiritVeinData，未成功生成候选不提供探测/灵气；手持许可、维度与同步由既有VeinCompassGameTests覆盖。

## 尚未完成

第15项仍需独立正常世界的固定种子自然生成采样、实际洞壁位置/矿量/罗盘对照、旧新边界原区块快照比对、重启重载与多人检查。本轮未调整脉率、币值或草药平衡。

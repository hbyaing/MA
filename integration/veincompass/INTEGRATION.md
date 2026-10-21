# 手持灵脉罗盘HUD

接线：新增唯一S2C `VeinCompassStatePacket`（构造器 `::new` / `::encode` / `::handle`），更新协议版本和ID表；合并本目录zh_cn/en_us各17条语言。服务器/客户端自动Forge subscribers，无GeneratedMod注册或物品改动。

服务器仅主/副手真实持有SpiritVeinCompassItem、存活非观战时每20tick发包。读取既有SpiritVeinData.nearest(pos,384)和auraAt(pos)，不做chunk加载或种子虚构探测。不修改SpiritVeinCompassItem右键输出及其40tick冷却。最近距离维持target现有三维规则；来源水平距离规则未覆盖目标。

客户端检查持有、存活、维度与60tick有效期；放下/死亡/退出/换维度后清空。HUD只展示八方方向、距离、阶数、当前浓度及品评；复用CultivationUi和真实罗盘图标。无独立灵地NBT、无第二修炼增益；CultivationLogic.environmentMultiplier原有浓度乘数及总上限保持唯一权威。

未运行构建或视觉验收。新增3项GameTest覆盖真实持有/现有数据快照、wire往返与方向/浓度边界。请根统一执行验证。

# 灵石与经济核对（第4项，2026-10-21）

规则沿用D01，不新增货币账户。SpiritStoneGrade定义下/中/上/极品=1/9/81/729下品；旧wuxingxiantu:spirit_stone就是下品。

| 入口 | 实际结算 | 核对与限制 |
|---|---|---|
| 合成兑换 | 六条spirit_stone_*_compress/split配方 | 相邻9合1、1拆9；本轮直接检查运行时加载配方的输入/输出价值和等级 |
| 背包付款 | economy/SpiritCurrency.pay | 主背包36槽复制试算，足额且找零可放入后才整体提交；余额不足或找零无空间不改背包；不使用储物袋/装备槽作为钱包 |
| 商人交易 | SpiritTradePayments及两处Mixin | 先模拟退回付款槽物品再预留实际价格；仅无NBT灵石A价且B空的报价适用；不把不支持的报价强改为灵石交易；已有SpiritTradeCurrencyGameTests含真实领取/取消路径 |
| 铭刻、精炼、重铸 | crafting/EquipmentCrafting.attempt | quote门槛后SpiritCurrency.pay，再扣其他材料与同一灵力；目标存于EquipmentWorkstationMenu独立input，未被主背包付款副本替换 |
| 认主、温养、本命 | crafting/EquipmentBond | 统一付款后重新获取主手对象；避免对已被付款复制替换的旧ItemStack写入 |
| 法宝辅材 | artifacts/ArtifactInfusion | 同样统一付款后重新取得主手；不叠加第二套主人/属性结算 |
| 诛仙剑阵 | formation/SwordArrayServer、SwordArrayUpkeep | 成阵240tick开始；每1200tick扣3645下品等值；补结经过区间、防重复tick；不足或无法放找零则关闭；这是已确认规则 |
| 九霄、万剑归宗 | arts/SustainedSwordArts.pay | 扣主能力CurrentMana/Aura同一灵力，不额外扣灵石；不擅自新增费用 |
| 炼丹/器炉配方 | AlchemyService、RefiningForgeBlockEntity | 当前并非额外背包灵石钱包扣费；配方材料/供灵/退款仍由第6–7项专项核对，不把所有炼制一概当作SpiritCurrency收费 |

本轮新增IntegrationEconomyGameTests：六条已加载兑换配方价值守恒；固定种子128组混合等级付款，检查成功后余额、无关NBT装备保持、失败付款整背包不变。既有测试另覆盖满背包找零失败、商人预留/取消、剑阵费用时序。

本轮未修改币值、价格或经济平衡。生存获取速度、UI余额显示、真实多人商人抢购与完整炼器失败退款仍需后续实机/生命周期验收。不得据此宣布第4项全部完成。

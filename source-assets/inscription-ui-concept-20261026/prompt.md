# 铭刻台优化参考图

使用内置 image_gen。仅生成设计参考，未修改运行时界面；用户批准后再提取素材、建立 UI 工作台与实装。

参考：用户提供的铭刻台截图（功能）、cultivation-ui-reference-split-v1/reference-original.png（背包主风格）、alchemy-ui-concept-20261012/alchemy-ui-v2.png（炼丹炉分区和控件）。明确排除储物袋 UI。

源码依据：EquipmentWorkstationScreen.java、EquipmentWorkstationMenu.java、EquipmentCrafting.java。真实可用铭纹为灵御、坚韧、轻身、御器、蓄势。采用灵御首次刻印为示例；需灵石4、玄铁1、青金石4、铭刻水晶1，灵力消耗0，首次成功率100%，灵韵70/25/5。材料从背包扣除，保留装备槽和旧纹凭据槽，行囊为27格加9格快捷栏。行囊物品与拥有数量为示意状态。

## 生图提示词

undefined

## 暗金配色修订

最终展示文件为 inscription-ui-dark-gold-reference.png。按用户要求改为墨黑、暖褐和古金，保留三行行囊加一行快捷栏。多轮内置生图修正后，四行已保留，但示意图每行仍多画一列（10列），不能作为槽位坐标规范。实装必须按 EquipmentWorkstationMenu 的9列、27格行囊+9格快捷栏配置，素材提取时校正。当前仅为待批准参考图，未实装，未建立工作台，未修改游戏资源。

本次只新增设计图与提示词，不运行 Mod 构建或自动安装；原有飞行测试和 docs/testing 未提交改动保留。

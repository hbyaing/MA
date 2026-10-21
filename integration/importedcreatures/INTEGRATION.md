# 来源黑皮野猪内容接入

来源固定为 hbyaing/xiantu r115 / ff253fa45ec919dc4fe9cc1d7587971d1f70a14c。只本地实施。

## 主代理接线

- `GeneratedMod` 构造函数注册事件发生前调用 `importedcreatures.ImportedCreatures.bootstrap()`；使用既有 `WuXingItems.ITEMS` 与 `ModEntities.ENTITY_TYPES`，不新设注册总线。
- 合并本目录 `zh_cn.json`、`en_us.json` 到正式语言文件，各16键。
- 将 `black_boar_spawn_egg`、`black_boar_king_spawn_egg` 加到 `WuXingItemSources.CREATIVE_TEST` 及相应已有创造测试标签。不给刷怪蛋生存配方。
- 炼丹成功结果在实际材料预扣后，经 `BeastMaterialEffects.infuseAlchemy(result, consumedMaterials)` 保存。只传实际消耗材料，别传炉中剩余输入；失败结果不注入。普通炉与天地为炉若各有独立结果创建入口，均调用此方法。
- `BasePillItem.finishUsingItem` 中 `PillEffects.apply(player,data,stack)` 返回成功后，调用 `BeastMaterialEffects.applyPill(player,stack)`；必须保留原境界、状态与药效检查。
- 普通炼器结算在 `forge.finishOperatedResult(job.result())` 后、`setPendingReward` 前执行 `operated=BeastMaterialEffects.infuseRefining(operated,job.materials())`。天地为炉独立炼器结算也使用其实际预扣材料调用相同方法。

## 保留内容及映射

- 两种敌对 Monster、近战/追击/受伤反击/游荡/浮水、猪音效、普通及王者属性原样保留。王者通过独立实体类型、较大碰撞体、1.45倍模型与额外后獠牙区分，不添加来源自绘血条，复用目标界面。
- 原来源2/5小层分别是练气中期/筑基中期，映射五行0/1大境界及中期标记，不引入来源15层玩家存档。
- 来源五种生境、普通权重5（1–2只）/王者权重1（1只）、草地及高亮度生成条件保留；按目标噬灵虫安全规则排除村庄及出生点32格。
- 玩家归因击杀掉普通精血1＋受掠夺影响随机0–3、毛皮1；王者精血2＋随机0–3、毛皮2。来源、土属性、强度和用途保存在版本化NBT；环境死亡不产出，复活魂仆不提供循环刷材料来源。
- 精血原用途是药物岩甲抗性辅材，不可直接服用。以真实消耗写入药物，服药时统一为原版抗性效果，和目标金刚丹按最大效果合并；普通/王者强度不同。数值依目标现有防御丹上限限制为抗性III，时长上限7200刻，不照搬来源可叠到V的免伤。
- 毛皮原用途是坚壁耐久词条及额外防御，分别接入既有`tough`铭纹与`WuxingCraft.BeastHideDefense`。受现有铭纹槽位限制；额外防御按实际使用最多+4，同装备再次加工取最大值，升级继承既有职业/认主数据。真实属性事件提供加成，非只有说明文字。
- 来源野猪继承Monster，没有额外繁殖、驯养、骑乘或独有护甲套装逻辑；不虚构这些入口。

## 当前可用配方

两个精血金刚丹配方使用既有金刚丹知识、境界、异火与成色。两个毛皮护甲配方使用既有炼器台、原材料预扣与职业结算。这些配方提供旧UI内的实际入口；原用途helper也可供之后已确认的器方使用，不导入第二套法宝词条树。

原生三栏八槽没有来源三个副药槽的自由注入角色；本批按真实丹方消耗注入精血，不擅自开放给任意丹方添加额外输入。四阶段新UI仍暂存。

## 资源及验证边界

直接复用来源BlackBoarModel、black_boar.png、精血mesh/PNG及毛皮模型/PNG；mesh加载器仅改包名与注册命名空间。来源资源不是重新绘制。五个GameTests覆盖真实近战归因、玩家击杀与环境掉落差异、实际SpawnPlacement、原生配方消耗及服药、护甲实际属性和升级归属继承。未在子代理运行Gradle；未做自然分布、客户端模型及多人实玩验收。

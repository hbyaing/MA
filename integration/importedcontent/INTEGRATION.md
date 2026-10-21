# 灵浴池与两种玩偶

来源 hbyaing/xiantu r115 / ff253fa45ec919dc4fe9cc1d7587971d1f70a14c。目标未发现同类方块或同名秘丹。

## 接线

- 在目标注册事件触发前调用 `importedcontent.ImportedContent.bootstrap()`，不新增注册总线。方块、方块实体及物品分别沿用 WuXingBlocks、ModBlockEntities、WuXingItems。
- 合并本目录 `zh_cn.json`、`en_us.json`，各12键。
- `laoyang_doll`、`chengran_doll`、`laoyang_secret_pill` 加现有创造测试分类及标签。源 ModTabs 103–104 实际提供两玩偶创造入口，源目录没有玩偶生存配方；不编造生存获取。秘丹仍可从已放置老杨玩偶取得，每人40刻一颗。
- MOD/Forge/客户端事件由包内订阅自动接入，无新网络消息或界面。

## 实际用途

灵浴池沿用八石砖＋炼药锅合成，四种原材料 `herb_juling/huayuan/xisui/pozhang` 各一株投入，保留实际药龄与其他NBT；重复物种/错误输入不消耗。四草使池成液，玩家持续入池200刻，按当前小境界门槛的2%＋平均药阶×1%补修为。调用目标 `CultivationLogic.gainCultivation`，继承道心锁、小境界上限及已有心法结算，绝不改境界、阶段、灵根或突破状态。一次四草只补益一人；离池/换境界/卸载中断进度，重复同刻调用不能加速。玩家已满或受锁时不消耗该池药材。拆池返还尚未消耗的原始药材；补全了源缺失的浴池自身掉落表以便回收。

目标 `CultivationDataImpl` 初始realm=0已是炼气；`PlayerLifecycleEvents.playerLogin` 通过 `PlayerInitialization.initializeIfNeeded` 初始化已有灵根，不存在源 stage0凡人层。故源浴池 mortal→stage1的60秒自动入道分支不适用，不迁入额外凡人层、不映射成目标筑基、不发放灵根。实际保留的是源已入道者200刻补修为分支。

老杨玩偶保留朝向、旋转、镜像、碰撞及原外观；主手右键每人每40服务端刻给一颗原秘丹，背包满则掉出，不因多个玩偶叠加速率。秘丹吃用32刻后严格 `nextBoolean` 二选一：50%再生X（600刻），50%中毒X（200刻）＋反胃（200刻），生存消耗一颗，创造保留。它不是金创丹/回春丹的替代品。

承然玩偶是源已登记、创造栏可取的装饰分支，沿用原皮肤、紫色名称、朝向与拆除自掉落；右键PASS，不产丹。

## 覆盖账本需同步的源遗留项

`item/RiftScrollItem.java` 当前是显式“Save compatibility only”的已移除法术壳：没有use、学习或施法入口，说明写明旧卷轴不再有用途，已从源创造栏/JEI隐藏。不要依据早期使用说明恢复一指裂地的地形破坏或巨量伤害。本批只记录为源遗留不可用功能，不声明已经迁入该法术。

## 验证

五个GameTests检查实际交互存料/重复拒收/存档与拆除返还、真实方块ticker满200刻修为封顶且根境不变、满修为和道心锁不消耗、40真实刻产丹与装饰分支、秘丹两种实际吃用结果。源原模型、皮肤、浴池液面及秘丹动画PNG/MCMeta直接复用，非重新绘制。子代理不运行Gradle；客户端模型、自然实玩与多人体验需由集中验收继续覆盖。

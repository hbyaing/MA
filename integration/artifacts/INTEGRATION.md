# 法宝来源资源接入

来源 src/main/resources/assets/mod_c1f9fd00，模型仅重映射 namespace，剑/锤 override 裁为有效 rank 1..3；PNG 原字节复制，逐文件 hash 见 source-assets.json。未引入停用法宝、rank 4..7 或额外成长规则。

- 剑/锤模型使用原生 elements，不需要 builtin/entity、BEWLR 或 mesh loader。ArtifactClient已为两物品注册 wuxingxiantu:artifact_rank 谓词，值为 ArtifactData.modelRank(stack)。
- 四护甲物品模型共用 artifact_armor。来源物品颜色分别为 armor 0xFFC0C8D8、leggings 0xFFB8C0D0、boots 0xFFA8B0C0、helmet 0xFFD0D8E8，ArtifactClient已通过RegisterColorHandlersEvent.Item适配。
- 穿戴沿用来源本来使用的原版 netherite_layer_1/2，target ArtifactArmorItem 已一致，无需复制游戏资产。
- zh_cn.json / en_us.json 是语言补丁，未自行合并总语言文件。D23/D24已确认：最大耐久与攻击用五行候选，护甲韧性独立且不叠次数。
- 尚未执行客户端或构建验收。

## 淬灵液接线

- `WuXingItems.QUENCH_ESSENCE` 注册为16堆叠；来源原生 `experience_bottle` 图标模型直接复用，无新增 PNG。保留来源水桶＋紫水晶碎片＋萤石粉→2瓶配方，原版合成负责返还桶。
- 仅原开炉者可在完整器台、有正确异火/境界、索引3“淬炼”阶段的首次操作中持液接受淬炼。普通/精准窗口均可，精准继续只作用于目标原成色体系。
- `CraftingJobs.reserveRefiningQuench` 将1瓶实际物品副本加入同一 `PendingRefining.materials` escrow，并给该job产物副本写唯一 `WuxingCraft.ArtifactQuenchConduct=3`。更新原持久任务并置 dirty 后该操作扣1瓶；失败校验不触碰手持物或操作位。
- 同一job重复淬灵不可接受；操作成功后原 `operationAttempts` 对应位也不可重放。无额外修为/货币/资源树。
- 原结算保留产物NBT；成功不返还materials。完整结构破坏时原结算会退款materials和灵力，故淬灵液随原料一并返还，且没有成品，无法同时免费拿成品与药液。
- `ArtifactData.conduct` 为唯一导灵合计入口：原基材/操控词条＋固定淬灵3。`ArtifactEffects.conduct` 委托它，原有回灵和雷击都读同一值。未触及耐久政策。
- 法宝 `ArtifactBootstrap` 已接入正式注册、六基础配方、十五来源辅材配方与客户端模型；淬灵只接受真实法宝产物，普通装备不吃药。当前统一构建/测试尚未完成，不提前宣称交付。
- 已补充真实GameTests、等待统一执行；仍需确认的生命周期：非原操作者/错误stage/无job/普通产物不扣液；stage3扣1且重复无效；job save/load保留1瓶escrow与+3；成功出单件法宝不退液；拆结构结算只退原料/1液不出法宝；回气/雷击实际读取相同conduct。

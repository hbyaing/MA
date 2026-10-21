# D28 蒲团游标冲关接线

- 两包须在 `NetworkChannel` 注册：`QteActionPacket` C2S、`QteStatePacket` S2C。均提供实例 `encode`、`FriendlyByteBuf` 构造、静态 `handle`。
- `meditationqte.client.QteClient` 自动 MOD 总线注册 K 键（来源 L 与原版进度界面冲突，K 不占目标已有默认绑定），Forge 总线处理客户端按键；`MeditationQte` 自动订阅服务端生命周期，不修改现有主界面。
- 合并本目录中英语言 JSON 到正式 lang。
- 根代理提供的 `CultivationDataImpl` 三 API：`meditationPenaltyTicks()`、`setMeditationPenaltyTicks(int)`、`tickMeditationPenalty()`。正增量在唯一 setter 结算减半，restore 不减半；模块每在线 END tick 扣一次，红区安装 18000 tick。勿再在 CultivationLogic 乘一次。
- 服务端每关随机 nonce，客户端只上报锁定动作；接收时间用服务端单调时钟计算位置，不接受客户端坐标或时间。服务端启动 tick 同时约束会话 600 tick；最多30秒单调时间；每关换 nonce。客户端估计整段 RTT（快照下行＋锁定上行）用于显示锁定到达时的游标，对判定没有权限。
- 永久尝试记录绑定目标 realm + cultivationStage，开始即消耗；取消、失败、登出不会退款。死亡复制尝试记录，瞬态会话不保存。
- 7 个 GameTests 覆盖真实蒲团/取消、三关奖励与时序重放、红区与唯一减半/保存、灰区与离座/换境界、心魔/离体/瘫痪/满修为拒绝、超时/登出以及两真实网络 codec。
- 尚需统一构建和真实客户端验收，本模块未自行执行构建。

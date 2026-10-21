# 已退役的异火缠绕外观

2026-09-28：按用户要求移除剑身缠绕、挥剑拖尾及命中/脱落碎焰。此目录不属于任何编译或资源打包 source set。

保留旧几何工具、渲染材质、贴图与对应几何测试，仅供未来重新设计时参考；不要直接复制回生产源码。旧完整效果可通过 deliverables 中 layered-v2 JAR 回看。原始模块素材和提示词仍在相邻的 weapon-flame-modules-v1 目录。

当前生产入口 WeaponFlameRenderer 只保留心剑共用的物品渲染桥接；WeaponFlameHitEffects 只保留协议 63 的无状态接收接口。受击生物点燃由 ExoticBurnRenderer / ExoticBurnRenderType 独立负责。

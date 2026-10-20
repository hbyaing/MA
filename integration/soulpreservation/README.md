# D29 元婴遁逃

独立 `soulpreservation/SoulPreservation` 自动订阅 Forge 事件，无需向主类新增注册。根协调者将本目录两份语言片段合并到正式语言文件即可。

- 来源：`upstream-xiantu/.../cultivation/NascentSoul.tryEscape`，仅自动致命伤保命、25 分钟冷却、6–14 格安全遁逃、8 秒隐身/抗性、10 秒加速/吸收、原地魂光与清仇恨。
- 目标权威：`LivingDamageEvent LOWEST` 在护甲、吸收和五行常规最终伤害之后判断，不移植来源近战增伤、破盾、法术暴击。
- 禁止绕过：`generic_kill`、虚空、绕过无敌伤害、渡劫及其 strike 标记、心境维度/session、主动元神出窍/移魂与已存在的本体。
- 世界持久冷却：使用既有 `PlayerCrafts` UUID 账户 `NascentSoulEscapeUntil` 与主世界 gameTime，不依赖玩家实体/背包/死亡克隆；服务器暂停时不计时，世界继续运行时离线计时。
- 落点：只读已加载区块，完整玩家包围盒在世界边界内且无碰撞/液体、脚下有支撑且非岩浆块等伤害方块；无安全落点则不发动、不消耗冷却。
- `SoulPreservationGameTests` 五项：真实伤害在有限五行护盾后保命、原版护甲先降低伤害、强制死亡/境界/出窍/渡劫/心境排除、保存及死亡替身账户冷却、落点阻挡/伤害地面/边界/未加载区块。

此代理按协调要求未构建；由协调者执行统一验证。

# 五行仙途 · 技能粒子库 v1

从当前源码提取目录，供制作功法、Boss、神通、符箓及系统特效使用。

## 直接使用

1. 打开 `preview/index.html`，可搜索中文名称/ID/源码程序，切换材质与配方，换色、暂停、拖动时间、调整数量和大小；直接双击可用，无 CDN、无服务器要求。
2. `resourcepack/assets/wuxingxiantu_vfx/textures/library/`：256×256 白色 RGBA 基础材质；动画为 512×512、4×4、每格128×128的16帧图集。透明边缘，按行从左到右再从上到下读取。
3. `catalog/skills.csv` / `catalog/skills.json`：逐技能源码、行号、描述、Boss 时序和程序片段；`recipes/`：32套建议配方。
4. Photon 1.1.17 / LDLib 1.0.42：把单独的资源包 ZIP 放入目标实例 resourcepacks 并启用；从 `photon/projects/` 选 `.fxproj` 在 Photon 编辑器打开。每个项目的材质库均包含全部60种材质。
5. `.fx` 位于资源包 `assets/wuxingxiantu_vfx/fx/`，包含60个循环单材质样片和32个有限组合样片。循环样片测试完需停止。

资源 ID 示例：`wuxingxiantu_vfx:materials/flame_tongue`、`wuxingxiantu_vfx:recipes/thunder`。在支持原生 Photon 命令的隔离测试场景中，可用 `/photon fx wuxingxiantu_vfx:recipes/thunder block X Y Z`，其中 X Y Z 应替换为真实坐标。停止同一位置的该效果：`/photon fx remove block X Y Z true wuxingxiantu_vfx:recipes/thunder`。命令格式依本地既有 Photon 接口记录；本次未进入游戏执行。

## 覆盖范围和状态

139项：18个普通功法、5个御剑剑技、7个神通、10个被动心法、77个Boss招式、6种符箓、9种异火、4个心剑仪式、3个系统特效。御剑5招已经包含在ArtCatalog的23项中，不重复计数。Boss为九位常规Boss加心魔共十位。

60种独立基础材质、32套组合配方；具体动画数量见 manifest.json。心法只提供被动/修炼外观设计；异火只提供新素材与源码配色。源码描述和新美术建议分字段保存；普通功法有些实际上按玩家周围AABB结算，不能根据描述文字臆造新的命中弹道。

**这是制作素材库，未接入当前模组技能。** 模组当前没有 Photon 依赖；资源包也不会凭空注册新的 Forge 粒子类型。Photon 示例由已有原生导出格式生成，完成离线 NBT 读写和引用检查；未做本轮 Minecraft 加载、可见播放、联机或性能实测。浏览器画面是材质构图预览，不是游戏录像。

Photon 组合样片使用 Billboard 粒子，用于选材、染色、发射和淡出调参；浏览器中的环绕、内吸、射线、落剑等是配方意图，尚未一比一转为游戏空间轨迹。真实地面法阵、光束、拖尾和晶柱应使用平面/几何/Beam/Trail或实体模型承载，不应靠一个贴图冒充完整三维技能。

范围不含全部生物AI普通攻击、七十二变逐实体动作。晶体素材是二维晶片，不是屏幕折射；镜片不是实时镜面。

## 制作约定

- 时间单位为tick；游戏常规20tick/s。Boss前摇、恢复和冷却是源码提取值；配方粒子寿命、数量、尺寸为美术建议。
- 每次真实子攻击调用一次配方；多段落点继续由BossSkillPrograms/HeartDemonSkillPrograms决定。现有BossCombatFx地面预警及安全缺口保持服务端几何一致。
- 金/木/水/火/土与10位Boss有独立色板；九种异火采用当前JSON的body/core/edge，不恢复旧退役外观。符箓六张符纹由源码16×32图样生成。
- 发光用SRC_ALPHA/ONE；烟、毒雾、叶、石片等用SRC_ALPHA/ONE_MINUS_SRC_ALPHA。保持深度测试，半透明层关闭深度写入；全亮并不意味着自动bloom。
- 白色RGB+straight alpha；不要再预乘一次。图集帧格有透明边距；禁止跨帧生成mipmap。多个发射器共享图集和材质。
- 建议低/中/高档数量35%/65%/100%，单个完整配方同时在场粒子上限128；48格后降档、96格后停发装饰。以上是设计预算，非测得性能结果。现有BossCombatFx的32粒子/tick预算需在实际接入时统一分配。
- 目标实体卸载、维度切换、施法取消、死亡时停止持续发射并清理句柄；伤害、击退、定身不由视觉层决定。

## 重建

项目中脚本：`tools/vfx_library/build.py`，依赖Python3、Pillow、NumPy、nbtlib==2.0.4。运行 `python tools/vfx_library/build.py`，再运行 `python tools/vfx_library/validate.py`。默认产物位于 `source-assets/skill-particle-library-v1/`。运行 `python tools/vfx_library/build.py --package` 可打包完整制作库和独立资源包。无Gradle步骤，不改动生产Java/资源。

程序化绘制的全部60种材质来自本轮确定性生成器，无AI图像生成调用，也未复用已退役火焰图片。Photon结构模板来自项目内既有原生导出，仅保留序列化结构。所有本轮新生成素材可随本项目编辑、打包和分发；不改变项目、Photon或LDLib本身的许可证。

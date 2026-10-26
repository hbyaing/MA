# 精铸台暗金 UI 接入与最终修订

对象仅为 jing_zhu_tai / REFORGE_TABLE，未使用炼器台配方界面。原精铸台参考图已获用户明确批准；随后用户限定只简化两套界面的背景，并降低边框对比度，布局、按钮、图标与四行行囊保持原位。本次按用户明确要求不使用 UI 工作台。

## 素材

reference.png 为确认图。manifest.json 记录25项资源的来源坐标与哈希；components/ 为可重复提取素材；tools/extract_reforge_dark_gold.py 同步运行时资源和采样元数据。clean-background-generated.png 为消除文字/装备遮挡的背景修复，flat-background-generated.png 为最终素色底。两者均使用内置 image_gen。背景提示词见 background-prompt.md，最终范围见 ../workstation-background-only-20261027/README.md。

标题从确认图直接裁取，避免重画字形。只给框线贴图施加0.48/0.46/0.42显示系数，每次复位，文字和物品不跟随变暗。控制图标与按钮皮肤沿用确认稿；重铸四种胚性使用正式游戏物品图标。行囊为3×9加独立1×9快捷栏，坐标与命中范围均保持固定。

## 功能

ReforgeScreen 使用现有 EquipmentWorkstationMenu 命令。覆盖稳炼、冒险、护器；锋锐、坚固、通灵、轻盈；待选新胚与原胚对照；采用或保留。保护用量由服务端同步护炼印替代标志与异火阶数，不重写消耗规则。待选期间禁止重复操作。未改变品质概率、费用或玩法判定。

## 验证

- reports/reforge-muted/reforge/result.json：15个原生视觉状态，包括普通、悬停、禁用、焦点、三种精炼、四种重铸、待选、缩放2/3/4、满阶和空装备。
- reports/inscription-muted/inscription/result.json：铭刻台7个状态与带JEI启动；精铸台最终素色验证为无JEI，之前带JEI的全部状态亦通过。
- 鼠标保护检查为0次捕获违规，仅测试客户端内部坐标，不使用OS指针操作。保护代码和夹具不进入正式包。
- reports/reforge-gametest-fixed.log：60项目标GameTests全通过，包括新菜单保护用量同步、待选阻塞与采用/保留交易。修复了旧铭刻测试缺少铭刻水晶、仍断言耗灵78的过期夹具；与现行耗灵0规则一致，未修改玩法规则。
- UI视觉夹具使用受控报价，不将它当作完整玩法验收；菜单交易由上述真实服务端GameTest验证。

此次仅做本范围验证，不声称项目整体验收完成。正式构建及安装清单见 reports/last-test-instance-installation.json。

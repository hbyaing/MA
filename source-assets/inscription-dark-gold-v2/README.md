# 铭刻台暗金 UI 提取与接入

用户确认暗金参考图，并于本次任务明确不使用 UI 工作台，随后授权直接接入。

## 素材

- reference.png：用户确认图；manifest.json：31项素材及来源坐标；components/：独立裁图。
- clean-background-generated.png：内置 image_gen 对被文字、物品遮挡背景的修复稿，保留山水和完整符阵。
- tools/extract_inscription_dark_gold.py：可重复提取原始24项和7项干净底板、按钮填充，并同步正式资源及采样元数据。
- 干净底板提示词：保持暗金山水、四角、各面板边界及圆形符阵；移除所有文字、数字、物品、铭纹图标、按钮、搜索框、进度条与行囊格，补回遮挡区域，符阵中心不含胸甲或装备框。不得添加新元素。

## 实装边界

独立 InscriptionScreen 使用正式裁图皮肤和游戏实时文字、物品、数量。五种可用铭纹与选中详情一致；材料仅显示实际四项，取消五行精华0/0。装备放大显示同时扩展命中区域。行囊固定3×9和独立1×9快捷栏。旧纹书槽遵守现有不可放入规则，有旧凭据时才显示兑换控件。精铸台尚未改动。

## JEI

修复普通测试启动时自动扫描 JEI 专用测试入口导致 IIngredientType 缺失：JeiLayoutCapture 仅在专项参数下编译，专项参数显式添加运行依赖。未修改 JEI 源码或 JAR。约定已记录于 AGENTS.md。

## 验证

隔离存档中的客户端视觉夹具覆盖普通、悬停、禁用、键盘焦点、搜索输入、GUI缩放2/3/4。鼠标捕获保护位于 myriadClientTest，运行时仅在测试客户端内部设置悬停坐标，不调用OS鼠标移动，不进入正式包。视觉夹具的材料与报价为受控状态，不代表完整端到端刻印玩法验收。

无JEI启动证据：reports/inscription-dark-gold/inscription/result.json；带JEI启动与实际原生悬停证据：reports/inscription-dark-gold-native/inscription/result.json。部件放大对照包括外框四角、标题衔接、搜索框、按钮与行囊格。最终构建及安装结果见项目构建日志和 reports/last-test-instance-installation.json。

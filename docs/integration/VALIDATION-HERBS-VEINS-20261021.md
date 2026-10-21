# 第5项灵脉与草药验证（2026-10-21）

基线d9a6159；本轮修改两处矿脉生产类，增加一个GameTest文件。范围及旧队列兼容边界见HERBS_VEINS_AUDIT.md。

- verifyFull完整构建成功，331/331必需GameTests通过，耗时2m21s；日志门禁通过，机器报告server-herbs-veins-result-20261021.json包含日志SHA256。
- 新增4项：旧保存不扩大地形写权限；生产生成算法在受控跨边界洞壁中只写获准区块；成熟多掉落只一株继承药龄/丹灰；同tick重种及保存保持药龄/丹灰上限。
- 18种来源草药的实际configured/placed/biome_modifier/loot/name静态资源链检查通过，记录herb-resource-audit-20261021.json。
- 源码打包检查4项通过；正式JAR未包含GameTests、fixtures或PointerProtection类；diff检查通过。
- 首次新增测试编译的var复合声明错误已修复；第二次测试的种子循环因坐标不满足固定候选网格卡住，已终止该测试进程，修正坐标并增加10000次上限，最终完整回归通过。失败日志保留，未作为通过证据。

最终原始日志：工作区work/myriad-herbs-veins-final-verifyFull.log。受控洞壁不是自然世界生成分布验收；未执行客户端、真实旧/新边界地形快照对照、磁盘进程重启与多人测试。保持开发进度快照定位。

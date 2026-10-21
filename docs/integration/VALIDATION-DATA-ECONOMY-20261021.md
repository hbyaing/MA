# 第2–4项核对与回归记录（2026-10-21）

基线86fbfce；本轮无src/main生产玩法改动。源码入口与未验收项见CHECKLIST-16.md，数据与经济边界分别见DATA_AND_SAVE_AUDIT.md、ECONOMY_AUDIT.md。

- verifyFull：327/327必需GameTests通过，BUILD SUCCESSFUL，1m59s；6项新增测试涵盖数据/主人边界与经济价值守恒。机器报告server-data-economy-result-20261021.json记录原始日志SHA256。
- 清洁构建clean verifyFull：325/325通过，BUILD SUCCESSFUL，3m27s（当时经济2项尚未进入该次编译，随后单独纳入上面的327项完整回归）。
- 编译警告：清洁构建119/119；远程基线130/119失败的11处过时API已用等价静态工厂替换；预算文件未修改。此处不是“零警告”声明，原有弃用积累仍在。
- 日志门禁6项单元测试通过；源码打包检查4项通过；16项清单中Java文件名引用均能在实际树中定位。
- 原始日志保留在工作区work/myriad-data-economy-final-verifyFull.log及work/myriad-data-audit-clean-verifyFull.log，远程基线失败日志保留myriad-remote-ci-failed.log。

未执行：客户端光影、D28–D30实机、自然世界生成、真实磁盘重载、原仙途旧档转换、多人综合验收。第3、4项为源码边界与服务端自动测试完成，不能标成整项全部实机通过。

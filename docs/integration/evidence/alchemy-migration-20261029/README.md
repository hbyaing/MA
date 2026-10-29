# 第12项验收证据

- `catalog-coverage.json`：注册材料、炉型、异火、配方、模板及公开规则覆盖。
- `import-mappings.json`：680组旧成色/药阶/元素/修为境界的固定导入结果。
- `reference-matrix.json.gz`：13,824组参考模拟的完整记录，gzip压缩JSON。
- `reference-summary.json`：参考品数分布与测得的引擎耗时，不是玩家成功概率。
- `start-result.json`、`resume-result.json`、`server-result.json`、`resume-server-result.json`：独立隐藏客户端与服务端校验；鼠标捕获违规必须为零。
- `ui-review.json`与PNG：真实界面、物品提示、GUI2/3/4、普通/悬停/焦点/禁用，10张独立部件裁图。
- `admin-no-benefit.json`：已圆满且灵力充足的管理角色实际使用导入创造丹，物品和可用剂数不扣除，不授予炼丹认证。
- `package-audit.json`、`installation.json`、`validation.json`：正式包、测试排除、磁盘安装和完整回归记录。

参考模拟是服务器管理员的离线诊断式计算，不生成物品、实践批次或认证。压缩矩阵可用Python标准库gzip与json读取。第13项多人、JEI、外部光影和任意故障/回滚组合未纳入本项完成声明。

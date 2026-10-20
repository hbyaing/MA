# 万界仙途 / Myriad Ascension

**开发进度快照，尚未完成整体验收。** 以五行仙途为主框架，仙途r115为内容补充。保留modId `wuxingxiantu`、老UI交付路线和既有统一账户；重复项由用户确认后取舍。

## 源码与协作

当前工作分支：`codex/myriad-integration-foundation-20261021`。老UI、新UI和早期整合快照分别保留，详见[恢复与分支说明](docs/integration/RECOVERY.md)。原始进度Release保持不变；原恢复包缺失历史父提交的问题已记录，不因SHA256或bundle verify通过而视为可完整恢复。

- [固定基线与依赖](docs/integration/acceptance-baseline.json)
- [验收清单](docs/integration/ACCEPTANCE.md)
- [已确认融合决策](docs/integration/accepted-decisions.json)
- [内容覆盖账本](SOURCE_CONTENT_COVERAGE.md)
- [项目协作规则](AGENTS.md)

## 构建和完整服务端验证

JDK17、Gradle8.8、ForgeGradle6.0.54、Minecraft1.20.1、Forge47.4.10、GeckoLib4.8.4；测试资源检查需要Python和Pillow>=10,<12。

```powershell
python -m pip install -r tools/requirements-qa.txt
.\gradlew.bat verifyFull -PpythonExecutable=python --console=plain
```

Linux/macOS使用`./gradlew`。首次解析依赖需联网；依赖已就绪时可使用`--offline`。服务端完整验证不能带`myriadExternalShaders`，客户端光影应单独运行。构建/编译成功不能代替本次GameTests明确完成且全部必需测试通过。

## 当前规则说明

- 剑阵维持费为每分钟5块极品灵石的等值金额，使用五行统一货币结算；当前5极品=3645下品价值。这是已确认方案。
- 剑阵护罩目前为视觉效果，未接实际防御或阵法伤害。
- 最新客户端、外部光影、自然生成、保存重载和多人验收各自记录，不能由历史255项测试或9项截图推导通过。
- 协作使用功能分支和PR，不强推覆盖，不丢弃其他人的改动，不将测试保护或QA代码打入正式jar。

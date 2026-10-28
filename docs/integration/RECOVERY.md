# 源码恢复与协作分支（2026-10-21）

原 progress-20261020 附件保持原样。它的SHA256正确，但cc6511d引用未包含的父对象7c04f0d，普通git clone不能完整恢复。

本次将三个原提交的**完全相同源码树**导入仓库已有main历史之后，形成无缺失父对象的可克隆分支。源码导入提交的SHA改变，源文件及原树SHA保持一致；逐项对应见 acceptance-baseline.json。原始进度包不删除、不覆盖，不宣称补齐了不存在的历史。

```sh
git clone --branch codex/myriad-ascension-delivery-old-ui https://github.com/hbyaing/Myriad-Ascension.git
cd Myriad-Ascension
```

- codex/myriad-ascension-delivery-old-ui：原058a4f0的老UI交付基线。
- codex/myriad-ascension-new-ui：原0430ef6的新UI独立快照，不合入老UI交付线。
- codex/myriad-ascension：原194d527的早期整合快照。
- codex/myriad-integration-foundation-20261021：本轮恢复/回归修复，通过PR申请合入main，禁止强推覆盖协作者提交。

新的恢复bundle将只包含上述完整来源分支和验收分支，不把破损历史头作为可恢复分支。应在空目录实际clone并执行git fsck验证，不以bundle verify单独作为完整性证明。

## 原恢复结论的修正（记录保留）

此前已核对原包SHA256并执行git bundle verify，但没有完成独立目录实际clone。因此“可直接完整恢复”的表述过强，应以实际克隆结果为准。原包的校验通过记录与缺失父提交问题同时保留，不用新包掩盖或删除问题。

验收层次分别记录：1.文件SHA256；2.bundle声明校验；3.独立空目录普通clone；4.克隆后git fsck；5.各分支源码树一致性。前两项不能替代后三项。

## 2026-10-28 仓库迁移

原仓库不可用后，源码迁至 https://github.com/hbyaing/Myriad-Ascension 。本地主线及可用历史分支保留原提交，最新炼丹 V5 第 07 项（accd114）包含于新主线。历史文档中的旧 PR、Release 链接作为历史记录保留，不能据此认定旧附件已恢复。详见 [迁移记录](REPOSITORY_MIGRATION_20261028.md)。

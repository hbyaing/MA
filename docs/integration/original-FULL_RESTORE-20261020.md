# 万界仙途全量进度恢复

完整历史包位于本仓库 Releases 的 `myriad-ascension-complete-progress.bundle`，包含全部已跟踪源码、原始素材、资源和三个开发分支；无外部前置提交要求。它不是增量补丁。

恢复：
```sh
git clone myriad-ascension-complete-progress.bundle Myriad-Ascension
cd Myriad-Ascension
git checkout codex/myriad-ascension-delivery-old-ui
```

当前交付路线提交：058a4f03bda7bc14e2c70f0eaa18d4097bc764a8。
新UI暂存在 codex/myriad-ascension-new-ui，不合入老UI交付。

完整包大小：479223838 字节。
SHA256：1c3be97136d33e66a5fb6ead91e3b5ff16f55dd187c7cc290139609540563726

下一步：先完成 NEXT_STEPS.md 所列的服务端GameTests，再单独进行光影客户端与自然生成验收。源码及测试代码编译通过，最新整体尚未正式验收。

浏览器上传限制使完整Git历史以Release附件保存，仓库当前根目录为交接/恢复入口；恢复包后即可得到完整可编辑工程与精确本地历史。构建缓存、运行存档、第三方依赖未纳入源码快照。

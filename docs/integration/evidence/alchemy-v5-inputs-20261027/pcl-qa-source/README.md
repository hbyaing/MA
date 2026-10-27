# 实际PCL验收夹具源码

此目录保存本机实际执行的独立QA包源码与构建初始化脚本。不是生产源码目录，不进入正式JAR；不得把它当作正式Mod发布。

本机执行：`gradlew reobfCodexPclQaJar --offline --init-script <qa.init.gradle>`。初始化脚本内的源码/输出绝对路径对应本次work目录，复跑须改为本机此目录路径；CreativeTabsCapture内输出路径同样须调整。夹具在独立新存档自动检查创造目录、两档GUI缩放和真实服务器取物包，完成后正常保存退出；鼠标抓取始终由NoPointerGrabMixin拦截。

结果文件及两档关键截图保存在上级。临时Jar已经从用户mods移走；正式包不包含这些类。

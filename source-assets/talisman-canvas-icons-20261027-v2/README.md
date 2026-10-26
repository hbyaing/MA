# 配套画布放大/缩小图标

使用内置 image_gen 生图，以原 expand.png 的暗金/墨色风格为参照。用户批准符号方案后反馈短翼、过长两版，最终按折中长度制作：翼约占图宽20%，源图中的箭头轮廓保留，不使用字体或运行时画线。

expand-generated.png 与 collapse-generated.png 为最终生图原件；对应提示词保存在同目录。正式素材49×49，在原按钮边框上提取生图的金色箭头，内部为不透明纯墨色、外缘沿用正式素材透明边角；普通与悬停态共享几何、采用配套金色。

可复现提取：`python tools/extract_talisman_canvas_icons.py`。运行文件为 talisman_v12 下 expand_v2、collapse_v2 及各自的 _hover 图片。旧16像素缩小图标不再使用。缩小控件20×20置于放大绘制右上角，操作/评分区继续保持在右下方。

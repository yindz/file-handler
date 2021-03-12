# 常用文件处理工具集
## 文件指纹批量修改器
### 用途
- 部分系统检测文件重复性时，实际上是比较文件的指纹信息(如MD5 Hash)
- 当文件内容发生变化(即使只有1个字节)后，文件的Hash也会发生变化
- 经过实验测试，往图片、视频、压缩包等类型文件末尾写入少量随机数据后，不会破坏文件内容，有较好的容错性
- 支持的格式：jpg/bmp/jpeg/gif/png/mp4/txt/zip/rar  
- 不适用于一些特殊的二进制文件(如可执行程序等)
### 范例
```
//源路径
String sourcePath = "D:\media";

//输出路径
String outputPath = "D:\output";

//初始化
FileHashHandler handler = new FileHashHandler(outputPath);

//批量处理源路径下所有可处理的文件
handler.submit(sourcePath);
```


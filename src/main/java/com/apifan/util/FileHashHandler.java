package com.apifan.util;

import com.github.freva.asciitable.AsciiTable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件指纹处理程序
 *
 * @author yin
 */
public class FileHashHandler extends BaseFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileHashHandler.class);

    //表头
    private static final String[] headers = {"File", "Size", "MD5", "New File", "New Size", "New MD5"};

    /**
     * 构造方法
     * <p>使用默认输出目录: user.home/output </p>
     */
    public FileHashHandler() {
    }

    /**
     * 构造方法
     * <p>使用自定义的输出目录路径</p>
     *
     * @param newPath 输出目录路径
     */
    public FileHashHandler(String newPath) {
        setOutputPath(newPath);
    }

    /**
     * 提交待处理的目录开始批量处理
     *
     * @param srcPath 待处理的源目录路径
     */
    public void submit(String srcPath) {
        if (StringUtils.isBlank(srcPath)) {
            logger.error("Source directory path is null or blank");
            return;
        }
        if (srcPath.equalsIgnoreCase(getOutputPath())) {
            //源目录和输出目录不能相同
            logger.error("Source directory can't be the same as the output directory");
            return;
        }
        if (!checkOutputPath()) {
            return;
        }

        logger.info("Checking source directory {}", srcPath);
        long begin = System.currentTimeMillis();

        File dir = new File(srcPath);
        if (!dir.exists()) {
            logger.error("Path {} does not exist", srcPath);
            return;
        }
        if (!dir.isDirectory()) {
            logger.error("Path {} does exists but is not a valid directory", srcPath);
            return;
        }
        List<String[]> outputMsgList = Collections.synchronizedList(new ArrayList<>());
        File[] fs = dir.listFiles();
        if (fs == null || fs.length == 0) {
            logger.warn("Source directory {} is empty", srcPath);
            return;
        }
        logger.info("{} item(s) detected", fs.length);

        //并行处理
        Arrays.stream(fs).parallel().forEach(f -> {
            if (f == null || !f.exists() || !f.isFile()) {
                return;
            }
            String fullPath = f.getAbsolutePath();
            String fullPathLower = fullPath.toLowerCase();
            if (fullPathLower.endsWith(".jpg")
                    || fullPathLower.endsWith(".jpeg")
                    || fullPathLower.endsWith(".bmp")
                    || fullPathLower.endsWith(".gif")
                    || fullPathLower.endsWith(".png")
                    || fullPathLower.endsWith(".zip")
                    || fullPathLower.endsWith(".rar")
                    || fullPathLower.endsWith(".txt")
                    || fullPathLower.endsWith(".mp4")) {
                try {
                    String[] returnInfo = processFile(fullPath, getOutputPath());
                    if (returnInfo != null) {
                        outputMsgList.add(returnInfo);
                    }
                } catch (IOException e) {
                    logger.error("Error occurred while handling {}", fullPath, e);
                }
            } else {
                logger.warn("File {} is ignored", fullPath);
            }
        });
        int successCount = outputMsgList.size();
        logger.info("Total cost: {} ms, {} file(s) generated", System.currentTimeMillis() - begin, successCount);
        if (!outputMsgList.isEmpty()) {
            System.out.println(AsciiTable.getTable(headers, outputMsgList.toArray(new String[outputMsgList.size()][6])));
        }
    }

    /**
     * 创建新文件
     * <p>往文件末尾写入随机的数据，达到更改文件哈希值的目的</p>
     *
     * @param src      原始文件
     * @param destPath 新文件的输出目录路径
     * @return 处理成功的文件信息
     * @throws IOException
     */
    private static String[] processFile(String src, String destPath) throws IOException {
        if (StringUtils.isBlank(src)) {
            return null;
        }
        File srcFile = new File(src);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return null;
        }
        File destFile = new File(destPath + File.separator + FilenameUtils.getName(src));
        FileUtils.copyFile(srcFile, destFile);

        int length = RandomUtils.nextInt(2, 1024);

        if(isBinaryFile(destFile)){
            //二进制文件则在末尾写入随机长度字节数据
            byte[] newBytes = new byte[length];

            //随机生成一个正整数n
            int n = RandomUtils.nextInt(2, 10);
            for (int i = 1; i < newBytes.length; i++) {
                if (i % n == 0) {
                    //能被n整除的位置，置为1
                    newBytes[i] = 1;
                }
            }
            //写入文件末尾
            FileUtils.writeByteArrayToFile(destFile, newBytes, true);
        } else {
            //文本文件则在末尾写入随机长度的空格
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(" ");
            }
            FileUtils.write(destFile, sb, StandardCharsets.UTF_8, true);
        }
        return new String[]{
                srcFile.getAbsolutePath(),
                String.valueOf(srcFile.length()),
                DigestUtils.md5Hex(new FileInputStream(srcFile)),
                destFile.getAbsolutePath(),
                String.valueOf(destFile.length()),
                DigestUtils.md5Hex(new FileInputStream(destFile))
        };
    }

    /**
     * 检测一个文件是否为二进制文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static boolean isBinaryFile(File file) throws IOException {
        String type = Files.probeContentType(file.toPath());
        logger.debug("file:{}, type:{}", file.getAbsolutePath(), type);
        if (type == null) {
            return true;
        } else return !type.startsWith("text");
    }
}

package com.apifan.handler;

import com.apifan.util.CommonUtils;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
     * 提交待处理的目录开始批量处理
     *
     * @param srcPath 待处理的源目录路径
     */
    public void submit(String srcPath) {
        if (!checkOutputPath()) {
            return;
        }
        long begin = System.currentTimeMillis();
        setIgnoreExts(Lists.newArrayList("exe", "bin", "iso", "jar", "dll", "so", "ocx", "a", "db", "dat", "key"));
        //执行处理
        execute(listSrcPath(srcPath), f -> {
            try {
                return processFile(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        int successCount = getOutputMsgList().size();
        logger.info("Total cost: {} ms, {} file(s) generated", System.currentTimeMillis() - begin, successCount);
        if (!getOutputMsgList().isEmpty()) {
            printResultTable(headers, getOutputMsgList().toArray(new String[getOutputMsgList().size()][6]));
        }
    }

    /**
     * 创建新文件
     * <p>往文件末尾写入随机的数据，达到更改文件哈希值的目的</p>
     *
     * @param srcFile  原始文件
     * @return 处理成功的文件信息
     * @throws IOException
     */
    private String[] processFile(File srcFile) throws IOException {
        if (srcFile == null) {
            return null;
        }
        if (!srcFile.exists() || !srcFile.isFile()) {
            return null;
        }
        File destFile = new File(getOutputPath() + File.separator + srcFile.getName());
        FileUtils.copyFile(srcFile, destFile);

        int length = RandomUtils.nextInt(2, 1024);
        if (CommonUtils.isBinaryFile(destFile)) {
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

}

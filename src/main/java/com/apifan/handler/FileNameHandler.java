package com.apifan.handler;

import com.apifan.util.CommonUtils;
import com.github.promeg.pinyinhelper.Pinyin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 文件名脱敏处理程序
 * 通过将文件名中的汉字转换成拼音的方式规避一些敏感词
 * 可随机插入噪点（使用全角字母）但不影响可读性
 *
 * @author yin
 */
public class FileNameHandler extends BaseFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileNameHandler.class);

    /**
     * 表头
     */
    private static final String[] headers = {"File", "New File"};

    /**
     * 是否加入噪音字符
     */
    private boolean addNoise;

    /**
     * 开启噪音
     */
    public void enableNoise() {
        this.addNoise = true;
    }

    /**
     * 关闭噪音
     */
    public void disableNoise() {
        this.addNoise = false;
    }

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

        //执行重命名
        execute(listSrcPath(srcPath), a -> {
            try {
                return rename(a);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        logger.info("Total cost: {} ms, {} file(s) renamed", System.currentTimeMillis() - begin, getOutputMsgList().size());
        if (!getOutputMsgList().isEmpty()) {
            printResultTable(headers, getOutputMsgList().toArray(new String[getOutputMsgList().size()][2]));
        }
    }

    /**
     * 将文件名中的汉字转成拼音
     *
     * @param srcFile 文件
     * @return 处理成功的文件信息
     * @throws IOException
     */
    private String[] rename(File srcFile) throws IOException {
        if (srcFile == null || !srcFile.exists() || !srcFile.isFile()) {
            return null;
        }
        String srcName = srcFile.getName();
        int length = srcName.length();
        StringBuilder newName = new StringBuilder();
        for (int i = 0; i < length; i++) {
            newName.append(convert(srcName.charAt(i)));
        }
        File destFile = new File(getOutputPath() + File.separator + newName);
        FileUtils.copyFile(srcFile, destFile);
        return new String[]{
                srcFile.getAbsolutePath(),
                destFile.getAbsolutePath()
        };
    }

    /**
     * 字符转换处理
     *
     * @param c 汉字
     * @return 拼音
     */
    private String convert(char c) {
        if (Pinyin.isChinese(c)) {
            //汉字转拼音
            String result = CommonUtils.toPinyin(c, true);
            if (addNoise) {
                //随机取1个字母转为全角字符作为噪点
                int i = RandomUtils.nextInt(0, result.length());
                String rand = Character.toString(result.charAt(i));
                return result.replace(rand, CommonUtils.half2Full(rand));
            }
            return result;
        } else {
            //非汉字保持原样
            return Character.toString(c);
        }
    }
}

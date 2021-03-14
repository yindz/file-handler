package com.apifan.util;

import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.tinypinyin.lexicons.java.cncity.CnCityDict;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 通用工具
 *
 * @author yin
 */
public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    static {
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance()));
    }

    /**
     * 汉字转拼音
     *
     * @param c          汉字字符
     * @param capitalize 首字母是否大写
     * @return 拼音
     */
    public static String toPinyin(char c, boolean capitalize) {
        String py = Pinyin.toPinyin(c).toLowerCase();
        return capitalize ? WordUtils.capitalize(py) : py;
    }

    /**
     * 半角字符转全角
     *
     * @param half 半角字符
     * @return 全角字符
     */
    public static String half2Full(String half) {
        if (StringUtils.isBlank(half)) {
            return half;
        }
        char[] ch = half.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == 32) {
                ch[i] = (char) 12288;
            } else if (ch[i] < 127) {
                ch[i] = (char) (ch[i] + 65248);
            }
        }
        return new String(ch);
    }

    /**
     * 检测一个文件是否为二进制文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean isBinaryFile(File file) throws IOException {
        String type = Files.probeContentType(file.toPath());
        logger.debug("file:{}, type:{}", file.getAbsolutePath(), type);
        if (type == null) {
            return true;
        } else return !type.startsWith("text");
    }
}

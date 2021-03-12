package com.apifan.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 基础文件处理器
 *
 * @author yin
 */
public class BaseFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(BaseFileHandler.class);

    /**
     * 文件输出路径
     */
    private String outputPath;

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        if (StringUtils.isBlank(outputPath)) {
            //默认输出目录: user.home/output
            this.outputPath = System.getProperty("user.home") + File.separator + "output";
            logger.info("The output directory is set to {}", this.outputPath);
        }
    }

    /**
     * 检查输出路径
     *
     * @return
     */
    public boolean checkOutputPath() {
        if (StringUtils.isBlank(outputPath)) {
            logger.error("Path is null or blank");
            return false;
        }
        File destDir = new File(outputPath);
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                logger.error("Path {} does exists but is not a valid directory", outputPath);
                return false;
            }
        } else {
            logger.info("Path {} does not exist, it will be created", outputPath);
            if (!destDir.mkdirs()) {
                logger.error("Can't create the output directory {}", outputPath);
                return false;
            }
        }
        return true;
    }
}

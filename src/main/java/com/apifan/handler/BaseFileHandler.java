package com.apifan.handler;

import com.github.freva.asciitable.AsciiTable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 基础文件处理器
 *
 * @author yin
 */
public class BaseFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(BaseFileHandler.class);

    /**
     * 输出信息列表
     */
    private final List<String[]> outputMsgList = Collections.synchronizedList(new ArrayList<>());

    /**
     * 文件输出路径
     */
    private String outputPath;

    /**
     * 需忽略的扩展名
     */
    private List<String> ignoreExts = new ArrayList<>();

    /**
     * 获取输出路径
     *
     * @return
     */
    public String getOutputPath() {
        if (StringUtils.isBlank(outputPath)) {
            //默认输出目录: user.home/output
            this.outputPath = System.getProperty("user.home") + File.separator + "output";
            logger.info("The output directory is set to {}", this.outputPath);
        }
        return outputPath;
    }

    /**
     * 设置输出路径
     *
     * @param outputPath
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * 设置需忽略的扩展名
     *
     * @param ignoreExts
     */
    public void setIgnoreExts(List<String> ignoreExts) {
        if (ignoreExts != null) {
            this.ignoreExts = ignoreExts;
        }
    }

    /**
     * 获取输出消息
     *
     * @return
     */
    public List<String[]> getOutputMsgList() {
        return outputMsgList;
    }

    /**
     * 多线程并行处理
     *
     * @param fs
     * @param handler
     */
    public void execute(File[] fs, Function<File, String[]> handler) {
        if (fs == null) {
            return;
        }
        Arrays.stream(fs).parallel().forEach(f -> {
            if (f == null || !f.exists() || !f.isFile()) {
                return;
            }
            if (ignoreExts != null && !ignoreExts.isEmpty()) {
                String ext = FilenameUtils.getExtension(f.getName().toLowerCase());
                if (StringUtils.isNotBlank(ext) && ignoreExts.contains(ext)) {
                    logger.warn("File {} is ignored", f.getAbsolutePath());
                    return;
                }
            }
            try {
                String[] returnInfo = handler.apply(f);
                if (returnInfo != null) {
                    getOutputMsgList().add(returnInfo);
                }
            } catch (Exception e) {
                logger.error("Error occurred while handling {}", f.getAbsolutePath(), e);
            }
        });
    }

    /**
     * 打印结果表格
     *
     * @param headers 表头
     * @param content 内容
     */
    public void printResultTable(String[] headers, String[][] content) {
        System.out.println(AsciiTable.getTable(headers, content));
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

    /**
     * 扫描出源路径下面的文件
     *
     * @param srcPath 源路径
     * @return
     */
    public File[] listSrcPath(String srcPath) {
        if (StringUtils.isBlank(srcPath)) {
            logger.error("Source directory path is null or blank");
            return null;
        }
        if (srcPath.equalsIgnoreCase(getOutputPath())) {
            //源目录和输出目录不能相同
            logger.error("Source directory can't be the same as the output directory");
            return null;
        }
        logger.info("Checking source directory {}", srcPath);
        File dir = new File(srcPath);
        if (!dir.exists()) {
            logger.error("Path {} does not exist", srcPath);
            return null;
        }
        if (!dir.isDirectory()) {
            logger.error("Path {} does exists but is not a valid directory", srcPath);
            return null;
        }
        getOutputMsgList().clear();
        File[] fs = dir.listFiles();
        logger.info("{} item(s) detected", fs != null ? fs.length : 0);
        if (fs == null || fs.length == 0) {
            throw new RuntimeException("Source directory " + srcPath + " is empty");
        }
        return fs;
    }
}

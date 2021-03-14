package com.apifan.util;

import com.apifan.handler.FileHashHandler;
import com.apifan.handler.FileNameHandler;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        FileHashHandler hashHandler = new FileHashHandler();
        hashHandler.setOutputPath("E:\\tmp\\new");
        hashHandler.submit("E:\\media");

        FileNameHandler nameHandler = new FileNameHandler();
        nameHandler.setOutputPath("E:\\tmp\\new");
        nameHandler.enableNoise();
        nameHandler.submit("E:\\tmp");
    }
}

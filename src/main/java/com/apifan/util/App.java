package com.apifan.util;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        FileHashHandler handler = new FileHashHandler("E:\\tmp\\output");
        handler.submit("E:\\media");
    }
}

package com.hjz.test.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/4/20
 */
public class TestChannelTransfer {

    public static void main(String[] args) {
        String separator = "\n";
        String[] fileList = {"D:\\project\\LeetCode\\src\\data1.txt", "D:\\project\\LeetCode\\src\\data2.txt"};
        try (FileChannel toChannel = new FileOutputStream("D:\\project\\LeetCode\\src\\data.txt", true).getChannel()) {
            for (String file : fileList) {
                FileOutputStream outputStream = new FileOutputStream(file, true);
                outputStream.write(separator.getBytes());
                outputStream.close();
                FileChannel fromChannel = new FileInputStream(file).getChannel();
                long size, left;
                size = left = fromChannel.size();
                while (left > 0) {
                    left -= fromChannel.transferTo(size - left, left, toChannel);
                }
                fromChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

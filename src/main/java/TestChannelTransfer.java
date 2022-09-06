import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/3/24
 */
@Slf4j
public class TestChannelTransfer {

//    public static void main(String[] args) {
//        String from = "helloworld/from.txt";
//        String to = "helloworld/to.txt";
//        long start = System.nanoTime();
//        try (FileChannel fromChannel = new FileInputStream(from).getChannel();
//             FileChannel toChannel = new FileOutputStream(to).getChannel()) {
//            // 效率高，底层会利用操作系统的零拷贝进行优化
//            fromChannel.transferTo(0, fromChannel.size(), toChannel);
//            // 二选一
//            //toChannel.transferFrom(fromChannel, 0, fromChannel.size());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.info("transferTo耗时：{}", (System.nanoTime() - start) / 1000000);
//    }

    public static void main(String[] args) {
        String from = "helloworld/from.txt";
        String to = "helloworld/to.txt";
        long start = System.nanoTime();
        try (FileChannel fromChannel = new FileInputStream(from).getChannel();
             FileChannel toChannel = new FileOutputStream(to).getChannel()) {
            // 效率高，底层会利用操作系统的零拷贝进行优化
            long size = fromChannel.size();
            for (long left = size; left > 0; ) {
                left -= fromChannel.transferTo(size - left, left, toChannel);
            }
            // 二选一
            //toChannel.transferFrom(fromChannel, 0, fromChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("transferTo耗时：{}", (System.nanoTime() - start) / 1000000);
    }

}

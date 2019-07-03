import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChnanelTest {
    public static void main(String[] args) throws IOException {

        FileInputStream fis = new FileInputStream("data.txt");
        //通过  getChannel 获取Channel 实例
        FileChannel channel = fis.getChannel();
        // 使用 allocate 创建个 48 个字节的 buffer
        ByteBuffer buf = ByteBuffer.allocate(48);
        // 使用 wrap 创建个 48 个字节的 buffer
        byte[] bytes = new byte[48];
        ByteBuffer buf2 = ByteBuffer.wrap(bytes, 0, bytes.length);
        int bytesRead = channel.read(buf);
        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead);
            buf.flip();
            while(buf.hasRemaining()){
                System.out.print((char) buf.get());
            }
            buf.clear();
            bytesRead = channel.read(buf);
            System.out.println("\n");
        }
        fis.close();

    }
}

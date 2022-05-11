import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

public class NIOClient {
    public static void main(String[] agrs) throws Exception {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 8888));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true)
        {
            int selectInt = selector.select();
            if (selectInt == 0)
                continue;
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext())
            {
                SelectionKey key = iterator.next();
                if (key.isConnectable())
                    handleConnect(key);
                if (key.isReadable())
                    handleRead(key);
                if (key.isWritable())
                    handleWrite(key);
                iterator.remove();
            }
        }
    }
    //可连接
    public static void handleConnect(SelectionKey key) throws Exception
    {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending())
            channel.finishConnect();
        channel.configureBlocking(false);
        channel.register(key.selector(), SelectionKey.OP_READ);
        sendInfo(channel, "客户端测试!");
    }

    public static void sendInfo(SocketChannel clientChannel, String msg) throws Exception {
        // 向客户端发送连接成功信息
        clientChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    //可读是当有数据的时候才会可读
    public static void handleRead(SelectionKey key) throws Exception {
        System.out.println("------------");
        SocketChannel channel = (SocketChannel) key.channel();
        String msg = "";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining())
                msg += new String(buffer.get(new byte[buffer.limit()]).array());
            System.out.println(msg);
            buffer.clear();
        }
        System.err.println("收到服务端消息:" + msg);
        channel.register(key.selector(), SelectionKey.OP_WRITE);
    }

    //只要tcp能写就是可写在大部分时间都是可写的因此会无限循环
    public static void handleWrite(SelectionKey key) throws Exception
    {
        System.err.println("客户端写数据!");
    }



}

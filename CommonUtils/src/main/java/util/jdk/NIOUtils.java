package util.jdk;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

/**
 * @author zhangtianyu0807
 */
public class NIOUtils {

    public static void copy(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        FileChannel channel = fileInputStream.getChannel();
        FileChannel outputChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while(channel.read(byteBuffer) != -1){
            byteBuffer.flip();
            outputChannel.write(byteBuffer);
            byteBuffer.clear();
        }
    }

    public static void main(String[] agrs) throws Exception {
        socketServerStart("127.0.0.1",8888);
    }




    public static void socketServerStart(String add,int port) throws Exception {
        //设置selector
        Selector selector = Selector.open();
        //打开通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(add,port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            //当没有io 准备会阻塞 有一个准备好就会执行
            int select = selector.select();
            if(select == 0){
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //通道有accept事件发生
                if (key.isValid() && key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    //如果设置是非阻塞就不会阻塞直接返回null
                    SocketChannel channel = server.accept();
                    registerChannel(selector, channel, SelectionKey.OP_READ);
                }

                //通道有read事件发生
                if (key.isValid() && key.isReadable()) {
                    readDataFromSocket(key);
                }

                //通道有write事件发生
                //key.isWritable()是表示Socket可写,网络不出现阻塞情况下,一直是可以写的,所认一直为true.一般我们不注册OP_WRITE事件.
                if (key.isValid() && key.isWritable()) {

                   // LOGGER.info("isWritable = true");
                    /*SocketChannel channel = (SocketChannel) key.channel();
                    String content = (String) key.attachment();
                    ByteBuffer byteBuffer = getByteBuffer(content);
                    channel.write(byteBuffer);*/
                    System.out.println("可写");

                }
                //通道有connect事件发生
                if (key.isValid() && key.isConnectable()) {
                    System.out.println("链接");
                }
                //移除已处理IO事件的通道
                iterator.remove();
            }
        }
    }

    /**
     * 处理监听IO操作，将接收到通道注册到Selector上
     * @param selector
     * @param channel
     * @param ops
     * @throws Exception
     */
    private static void registerChannel(Selector selector, SelectableChannel channel, int ops) throws Exception {
        if (channel == null) {
            return;
        }
        channel.configureBlocking(false);
        channel.register(selector, ops);
    }


    /**
     * 处理读取IO操作，将服务端接收到的数据发送回客户端
     * @param key
     * @throws Exception
     */
    private static void readDataFromSocket(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int count;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        String content ="";
        while ((count = socketChannel.read( buffer)) > 0) {
            buffer.flip();
            // 通过 buffer.array() 拿到buffer中的数组（没必要buffer.filp）
            content= content+new String((buffer).array(), 0, 1024);
            socketChannel.write(buffer);
            // 将当前SocketChannel再注册上WRITE事件
            buffer.clear();
        }
        if (count < 0) {
            // Close channel on EOF, invalidates the key
            socketChannel.close();
        }
    }

    /**
     * string转buffer
     * @param str
     * @return
     */
    public static ByteBuffer getByteBuffer(String str) {
        return ByteBuffer.wrap(str.getBytes());
    }

    /**
     *  byteBuffer->string
     * @param buffer
     * @return
     */
    public static String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            //用这个的话，只能输出来一次结果，第二次显示为空
            // charBuffer = decoder.decode(buffer);
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }

    /**
     * 入参1 源文件 入参2 目标文件
     * 单纯通道复制
     */
    public static void nioTransform(File src, File dst){
        try(
            FileInputStream fileInputStream = new FileInputStream(src);
            FileOutputStream fileOutputStream = new FileOutputStream(dst);
        ) {
            FileChannel readChannel = fileInputStream.getChannel();
            FileChannel writeChannel = fileOutputStream.getChannel();
            readChannel.transferTo(0,readChannel.size(),writeChannel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

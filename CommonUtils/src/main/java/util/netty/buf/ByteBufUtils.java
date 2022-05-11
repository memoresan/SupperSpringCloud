package util.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import util.jdk.NIOUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteBufUtils {

    public static void main(String[] agrs){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("zty".getBytes());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        System.out.println(new String(byteBuf.array()));
        while(byteBuf.readableBytes() !=0){
            System.out.println(byteBuf.readByte());
        }

    }


    public static ByteBuf strTobuf(String str){
        Charset charset = Charset.forName("UTF-8");
        ByteBuf buf1 = Unpooled.copiedBuffer(str, charset);  //← --  创建一个用于保存给定字符串的字节的ByteBuf
        return buf1;
    }

    public static String bufToString(ByteBuf buf){
        if (buf.hasArray()) {//检查buf是否支持一个数组
            byte[] array = buf.array();
            return new String(array);
            //或者直接toArray
        }else{
            ByteBuffer byteBuf = buf.nioBuffer();
            String str = NIOUtils.getString(byteBuf);
            return str;
        }
    }


    //消息消费完成后自动释放 读完要释放如果不想手动释放 集成SimpleChannelInboundHandler
    private void release(Object msg) {
        try {
            ReferenceCountUtil.release(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

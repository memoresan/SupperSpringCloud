package util.netty.http;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import util.netty.buf.ByteBufUtils;

import java.net.URI;

public class HttpCliHeader extends ChannelInboundHandlerAdapter {
    private String content;
    private URI url;

    public HttpCliHeader(String content, URI url) {
        this.content = content;
        this.url = url;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, url.toASCIIString(), Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8")
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        // 2.发送
        // 注意必须在使用完之后，close channel
        ctx.writeAndFlush(request);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpResponse){
            FullHttpResponse response = (FullHttpResponse)msg;
            int code = response.status().code();
            ByteBuf content = response.content();
            System.out.println(ByteBufUtils.bufToString(content));
        }
    }
}

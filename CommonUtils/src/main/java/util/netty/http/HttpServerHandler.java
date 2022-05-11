package util.netty.http;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;


public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest){
            FullHttpRequest req = (FullHttpRequest)msg;
            try {
                String uri = req.uri();
                // 2.获取请求体
                ByteBuf buf = req.content();
                String content = buf.toString(CharsetUtil.UTF_8);
                System.out.println(buf);
                // 3.获取请求方法
                HttpMethod method = req.method();
                // 4.获取请求头
                HttpHeaders headers = req.headers();
                if(method.equals(HttpMethod.POST)){
                    // 接收用户输入，并将输入返回给用户
                    Content c = new Content();
                    c.setUri(uri);
                    c.setContent(content);
                    response(ctx, c);
                }
            }catch (Exception e){

            }finally {
                req.release();
            }
        }
    }
    private void response(ChannelHandlerContext ctx, Content c) {

        // 1.设置响应
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(JSONObject.toJSONString(c), CharsetUtil.UTF_8));

        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");

        // 2.发送
        // 注意必须在使用完之后，close channel
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}

class Content{
    String uri;
    String content;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
package util.netty.server.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyServerOutHander1 extends ChannelOutboundHandlerAdapter {




    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerOutHander1-read");
        super.read(ctx);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("进来了---NettyServerOutHander1 bind");
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("进来了---NettyServerOutHander1 connect");
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //当你执行write的时候就会调用invokeWrite方法，next.invokeWrite(msg, promise);
        System.out.println("进来了--- NettyServerOutHander1 write:"+Thread.currentThread().getName());
        ctx.write(msg, promise);
    }

   /* @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().schedule(()->{
            ctx.channel().write("hello");
        },3, TimeUnit.SECONDS);
    }*/
}

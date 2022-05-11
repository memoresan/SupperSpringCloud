package util.netty.server.channel;

import io.netty.channel.*;

import java.net.SocketAddress;

public class NettyServerOutHander extends ChannelOutboundHandlerAdapter {




    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerOutHander-read");
        super.read(ctx);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("进来了---NettyServerOutHander bind");
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("进来了---NettyServerOutHander connect");
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //当你执行write的时候就会调用invokeWrite方法，next.invokeWrite(msg, promise);
        System.out.println("进来了--- NettyServerOutHander write");
        promise.addListener(x->{
           System.out.println("listener进来了");
        });
        ctx.write(msg, promise);
    }
}

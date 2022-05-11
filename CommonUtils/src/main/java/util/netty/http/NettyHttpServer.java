package util.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyHttpServer {
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    public static void main(String[] agrs){
        start();
    }

    public static void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            ServerBootstrap serverBootstrap = bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道初始化对象(匿名对象)
                    //ChannelInitializer 会有两次执行第一次的执行是serverbootstrapAdapter 并且进入他的init方法
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //System.out.println("客户socketchannel hashcode=" + ch.hashCode()); //可以使用一个集合管理 SocketChannel， 再推送消息时，可以将业务加入到各个channel 对应的 NIOEventLoop 的 taskQueue 或者 scheduleTaskQueue
                        // 解码成HttpRequest
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        // 解码成FullHttpRequest
                        //当我们用POST方式请求服务器的时候，对应的参数信息是保存在message body中的,如果只是单纯的用HttpServerCodec是无法完全的解析Http POST请求的，
                        // 因为HttpServerCodec只能获取uri中参数，所以需要加上HttpObjectAggregator。
                        ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                        ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
                        // 添加WebSocket解编码
                        //pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                        ch.pipeline().addLast(new HttpServerHandler());

                    }
                });
                //绑定一个端口并且同步, 生成了一个 ChannelFuture 对象
                //启动服务器(并绑定端口)
                ChannelFuture cf = serverBootstrap.bind(6668).sync();

                //给cf 注册监听器，监控我们关心的事件

                cf.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            System.out.println("监听端口 6668 成功");
                        } else {
                            System.out.println("监听端口 6668 失败");
                        }
                    }
                });
                //对关闭通道进行监听,如果没有这句话就会直接往下走关闭了
                cf.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }

    }

    /**
     * 手动关闭netty服务
     */
    public static void close(){
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
        }else if(workerGroup != null){
            workerGroup.shutdownGracefully();
        }
    }

}

package com.rpc.netty.server;

import com.rpc.RpcServer;
import com.rpc.codec.CommonDecoder;
import com.rpc.codec.CommonEncoder;
import com.rpc.serializer.JsonSerializer;
import com.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer implements RpcServer {
    private static final Logger logger= LoggerFactory.getLogger(NettyServer.class);

    /*
    Netty 中有一个很重要的设计模式——责任链模式，责任链上有多个处理器，每个处理器都会对数据进行加工，
    并将处理后的数据传给下一个处理器。代码中的 CommonEncoder、CommonDecoder和NettyServerHandler 分别就是编码器，
    解码器和数据处理器。因为数据从外部传入时需要解码，而传出时需要编码，类似计算机网络的分层模型，
    每一层向下层传递数据时都要加上该层的信息，而向上层传递时则需要对本层信息进行解码。

    ********channel:一种连接到网络套接字或能进行读、写、连接和绑定等I/O操作的组件。**************
            channel为用户提供：
                1.通道当前的状态（例如它是打开？还是已连接？）
                2.channel的配置参数（例如接收缓冲区的大小）
                3.channel支持的IO操作（例如读、写、连接和绑定），以及处理与channel相关联的所有IO事件和请求的ChannelPipeline。
     */
    @Override
    public void start(int port) {
        //创建两个线程组 boosGroup、workerGroup
        /*
        bossGroup 用于监听客户端连接，专门负责与客户端创建连接，并把连接注册到workerGroup的Selector中。
        workerGroup用于处理每一个连接发生的读写事件。
        相当于c++网络编程中主线程负责监听事件，子线程处理具体的逻辑
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建服务端的启动对象，设置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //设置两个线程组boosGroup和workerGroup
            serverBootstrap.group(bossGroup,workerGroup)
                    //设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG,256)
                    //设置保持活动连接状态
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //使用匿名内部类的形式初始化通道对象
                    /*
                    option()设置的是服务端用于接收进来的连接，也就是boosGroup线程。
                    childOption()是提供给父管道接收到的连接，也就是workerGroup线程。
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //给pipeline管道设置处理器,这些处理都是交给workerGroup去操作的
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });//给workerGroup的EventLoop对应的管道设置处理器

            //绑定端口号，启动服务端
            //ChannelFuture提供操作完成时一种异步通知的方式。一般在Socket编程中，
            // 等待响应结果都是同步阻塞的，而Netty则不会造成阻塞，
            // 因为ChannelFuture是采取类似观察者模式的形式进行获取结果。
            /*
            例如：
               //添加监听器
channelFuture.addListener(new ChannelFutureListener() {
    //使用匿名内部类，ChannelFutureListener接口
    //重写operationComplete方法
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        //判断是否操作成功
        if (future.isSuccess()) {
            System.out.println("连接成功");
        } else {
            System.out.println("连接失败");
        }
    }
});
             */
            //bind():提供用于服务端或者客户端绑定服务器地址和端口号，默认是异步启动。如果加上sync()方法则是同步。
            ChannelFuture future = serverBootstrap.bind(port).sync();

            //对关闭通道进行监听
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时有错误发生:",e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

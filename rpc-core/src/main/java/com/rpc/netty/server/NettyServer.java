package com.rpc.netty.server;

import com.rpc.RpcServer;
import com.rpc.codec.CommonDecoder;
import com.rpc.codec.CommonEncoder;
import com.rpc.serializer.JsonSerializer;
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

     */
    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG,256)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new CommonEncoder(new JsonSerializer()));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时有错误发生:",e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

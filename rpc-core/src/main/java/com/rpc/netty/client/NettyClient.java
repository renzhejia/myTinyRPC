package com.rpc.netty.client;

import com.rpc.RpcClient;
import com.rpc.codec.CommonDecoder;
import com.rpc.codec.CommonEncoder;
import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.serializer.JsonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String host;
    private int port;
    private static final Bootstrap bootstrap;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static {
        //创建bootstrap对象，配置参数
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        //设置线程组
        bootstrap.group(group)
                //设置客户端的通道实现类型
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                //使用匿名内部类初始化通道
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new CommonDecoder())
                                .addLast(new CommonEncoder(new JsonSerializer()))
                                //添加客户端通道的处理器
                                .addLast(new NettyClientHandler());
                    }
                });
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try{
            //连接服务端
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务器 {}:{}", host, port);
            Channel channel = future.channel();
            if(channel!=null){
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                    }else{
                        logger.error("发送消息时有错误发生: ", future1.cause());
                    }
                });
                //对通道关闭进行监听
                channel.closeFuture().sync();
                /*
                在静态代码块中就直接配置好了 Netty 客户端，等待发送数据时启动，channel 将 RpcRequest 对象写出，
                并且等待服务端返回的结果。注意这里的发送是非阻塞的，所以发送后会立刻返回，而无法得到结果。
                这里通过 AttributeKey 的方式阻塞获得返回结果：
                 */
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }
        }catch (InterruptedException e){
            logger.error("发送消息时有错误发生: ", e);
        }
        return null;
    }
}

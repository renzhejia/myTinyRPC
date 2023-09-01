package com.rpc.netty.server;

import com.rpc.RequestHandler;
import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.registry.DefaultServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义的Handler需要继承Netty规定好的HandlerAdapter
 * 才能被Netty框架所关联，有点类似SpringMVC的适配器模式
 **/
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private static ServiceRegistry serviceRegistry;

    static {
        requestHandler = new RequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }


    /**
     * 处理方式和 Socket 中的逻辑基本一致
     * @param channelHandlerContext:上下文对象，可以拿到channel、pipeline等对象，方便进行读写等操作。
     * @param rpcRequest:统一设置的rpc请求体
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        try {
            /*
            ********channel:一种连接到网络套接字或能进行读、写、连接和绑定等I/O操作的组件。**************
            channel为用户提供：
                1.通道当前的状态（例如它是打开？还是已连接？）
                2.channel的配置参数（例如接收缓冲区的大小）
                3.channel支持的IO操作（例如读、写、连接和绑定），以及处理与channel相关联的所有IO事件和请求的ChannelPipeline。
                *
                * 这里channel实现了写操作
                *
             */
            logger.info("服务器接收到请求:", rpcRequest);
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = requestHandler.handler(rpcRequest, service);
            ChannelFuture future = channelHandlerContext.writeAndFlush(RpcResponse.success(result));
            future.addListener(ChannelFutureListener.CLOSE);
        }finally {
            ReferenceCountUtil.release(rpcRequest);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}

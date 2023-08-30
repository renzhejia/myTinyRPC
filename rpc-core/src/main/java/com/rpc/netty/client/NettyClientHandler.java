package com.rpc.netty.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);


    /**
     * 这里只需要处理收到的消息，即 RpcResponse 对象，由于前面已经有解码器解码了，这里就直接将返回的结果放入 ctx 中即可。
     * @param channelHandlerContext
     * @param rpcResponse
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        try{
            logger.info(String.format("客户端接收到消息: %s", rpcResponse));
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            channelHandlerContext.attr(key).set(rpcResponse);
            channelHandlerContext.channel().close();
        }finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}

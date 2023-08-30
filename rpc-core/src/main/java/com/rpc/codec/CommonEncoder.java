package com.rpc.codec;

import com.rpc.entity.RpcRequest;
import com.rpc.enumeration.PackageType;
import com.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 通用的编码拦截器
 * @author ziyang
 *
 * 自定义的协议格式:
 * 首先是 4 字节魔数，表识一个协议包。接着是 Package Type，标明这是一个调用请求还是调用响应，
 * Serializer Type 标明了实际数据使用的序列化器，这个服务端和客户端应当使用统一标准；Data Length 就是实际数据的长度，
 * 设置这个字段主要防止粘包，最后就是经过序列化后的实际数据，可能是 RpcRequest 也可能是 RpcResponse 经过序列化后的字节，
 * 取决于 Package Type。

 * +---------------+---------------+-----------------+-------------+
 * |  Magic Number |  Package Type | Serializer Type | Data Length |
 * |    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
 * +---------------+---------------+-----------------+-------------+
 * |                          Data Bytes                           |
 * |                   Length: ${Data Length}                      |
 * +---------------------------------------------------------------+

 */

/**
 * CommonEncoder 继承了MessageToByteEncoder 类，见名知义，就是把 Message（实际要发送的对象）转化成 Byte 数组。
 * CommonEncoder 的工作很简单，就是把 RpcRequest 或者 RpcResponse 包装成协议包。 根据上面提到的协议格式，
 * 将各个字段写到管道里就可以了，这里serializer.getCode() 获取序列化器的编号，
 * 之后使用传入的序列化器将请求或响应包序列化为字节数组写入管道即可
 *
 */
public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * CommonEncoder 继承了MessageToByteEncoder 类，重写了MessageToByteEncoder中的encode抽象方法
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER);
        if(msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        out.writeInt(serializer.getCode());
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

}

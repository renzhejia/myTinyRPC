package com.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.SerializerCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements CommonSerializer{

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);
    /*
    Kryo 可能存在线程安全问题，文档上是推荐放在 ThreadLocal 里，一个线程一个 Kryo。
    在序列化时，先创建一个 Output 对象（Kryo 框架的概念），接着使用 writeObject
    方法将对象写入 Output 中，最后调用 Output 对象的 toByte() 方法即可获得对象的字节数组。
    反序列化则是从 Input 对象中直接 readObject，这里只需要传入对象的类型，
    而不需要具体传入每一个属性的类型信息。
     */

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL=ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output,obj);
            KRYO_THREAD_LOCAL.remove();
            return output.toBytes();
        }catch (Exception e){
            logger.error("序列化时有错误发生:",e);
            throw new RuntimeException();
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)){
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            Object obj = kryo.readObject(input, clazz);
            KRYO_THREAD_LOCAL.remove();
            return obj;
        }catch (Exception e){
            logger.error("反序列化时有错误发生:",e);
            return new RuntimeException();
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("KRYO").getCode();
    }
}

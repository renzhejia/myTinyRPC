package com.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpc.enumeration.SerializerCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rpc.entity.RpcRequest;

import java.io.IOException;

/**
 * 使用JSON格式的序列化器
 * @author ziyang
 */
public class JsonSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("序列化时有错误发生: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if(obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            logger.error("反序列化时有错误发生: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
        这里由于使用JSON序列化和反序列化Object数组，无法保证反序列化后仍然为原实例类型
        需要重新判断处理

        JSON 序列化工具我使用的是 Jackson，在 pom.xml 中添加依赖即可。序列化和反序列化都比较循规蹈矩，
        把对象翻译成字节数组，和根据字节数组和 Class 反序列化成对象。这里有一个需要注意的点，
        就是在 RpcRequest 反序列化时，由于其中有一个字段是 Object 数组，在反序列化时序列化器会根据字段类型进行反序列化，
        而 Object 就是一个十分模糊的类型，会出现反序列化失败的现象，这时就需要 RpcRequest 中的另一个字段 ParamTypes
        来获取到 Object 数组中的每个实例的实际类，辅助反序列化，这就是 handleRequest() 方法的作用。
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        for(int i = 0; i < rpcRequest.getParamTypes().length; i ++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();
    }

}

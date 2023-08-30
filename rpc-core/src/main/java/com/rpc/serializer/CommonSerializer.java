package com.rpc.serializer;

/**
 * 通用的序列化反序列化接口
 *
 * 四个方法，序列化，反序列化，获得该序列化器的编号，已经根据编号获取序列化器
 * @author ziyang
 */
public interface CommonSerializer {

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }

}

package com.rpc;

import com.rpc.serializer.CommonSerializer;

public interface RpcServer {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    //像nacos注册服务
    <T> void publishService(T service, Class<T> serviceClass);
}

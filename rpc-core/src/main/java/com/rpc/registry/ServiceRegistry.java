package com.rpc.registry;

import java.net.InetSocketAddress;

/**
 * nacos远程服务注册表通用接口
 * @author ziyang
 */
public interface ServiceRegistry {
    //将服务的名称和地址注册进服务注册中心
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    //根据服务名称从注册中心获取到一个服务提供者的地址
    InetSocketAddress lookupService(String serviceName);
}

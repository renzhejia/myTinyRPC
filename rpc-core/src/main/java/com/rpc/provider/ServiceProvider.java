package com.rpc.provider;
/**
 * 服务注册表通用接口
 * @author ziyang
 */
public interface ServiceProvider {
    /**
     * 将一个服务注册进注册表
     * @param service 待注册的服务实体
     * @param <T> 服务实体类
     */
    <T> void addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 根据服务名称获取服务实体
     * @param serviceName 服务名称
     * @return 服务实体
     */
    Object getServiceProvider(String serviceName);
}

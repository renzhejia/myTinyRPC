package com.rpc.registry;

/**
 * 默认的服务注册表
 * @author ziyang
 */
public class DefaultServiceRegistry implements ServiceRegistry {
    @Override
    public <T> void register(T service) {

    }

    @Override
    public Object getService(String serviceName) {
        return null;
    }
}

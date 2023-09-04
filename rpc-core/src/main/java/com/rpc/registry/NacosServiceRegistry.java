package com.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.loadbalaner.LoadBalancer;
import com.rpc.loadbalaner.RandomLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosServiceRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    private static final String SERVER_ADDR = "192.168.111.1:8847";
    private static final NamingService namingService;

    private final LoadBalancer loadBalancer;

    public NacosServiceRegistry(){
        this.loadBalancer=new RandomLoadBalancer();
    }

    public NacosServiceRegistry(LoadBalancer loadBalancer){
        if(loadBalancer==null){
            this.loadBalancer=new RandomLoadBalancer();
        }else{
            this.loadBalancer=loadBalancer;
        }
    }

    static {
        try{
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        }catch (NacosException e){
            logger.error("连接到nacos时有错误发生:",e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }


    //将服务的名称和地址注册进服务注册中心
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try{
            namingService.registerInstance(serviceName,inetSocketAddress.getHostName(),inetSocketAddress.getPort());
        }catch (NacosException e){
            logger.error("服务注册的时候有错误发生:",e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

    //根据服务名称从注册中心获取到一个服务提供者的地址
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try{
            List<Instance> instances = namingService.getAllInstances(serviceName);
            //这里新增了nacos的负载均衡策略，此时不需要每次都取第一个服务者了
            //Instance instance = instances.get(0);
            Instance instance=loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(),instance.getPort());
        }catch (NacosException e){
            logger.error("获取服务时有错误发生:",e);
        }
        return null;
    }
}

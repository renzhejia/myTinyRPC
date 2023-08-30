package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.netty.server.NettyServer;
import com.rpc.registry.DefaultServiceRegistry;
import com.rpc.registry.ServiceRegistry;

public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(helloService);
        NettyServer server = new NettyServer();
        server.start(9999);
    }
}

package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.netty.server.NettyServer;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.provider.ServiceProvider;
import com.rpc.registry.ServiceRegistry;

public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer server = new NettyServer("127.0.0.1",9999);
        server.publishService(helloService,HelloService.class);
    }
}

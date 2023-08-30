package com.rpc.test;

import com.rpc.RpcServer;
import com.rpc.api.HelloService;
import com.rpc.registry.DefaultServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import com.rpc.socket.server.SocketServer;


public class SocketTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(helloService);
        SocketServer socketServer = new SocketServer(registry);
        socketServer.start(9000);
    }
}

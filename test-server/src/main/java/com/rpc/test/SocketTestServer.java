package com.rpc.test;

import com.rpc.api.HelloService;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.ServiceRegistry;
import com.rpc.serializer.CommonSerializer;
import com.rpc.socket.server.SocketServer;


public class SocketTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketServer socketServer = new SocketServer("127.0.0.1", 9998, CommonSerializer.DEFAULT_SERIALIZER);
        socketServer.publishService(helloService,HelloService.class);
    }
}

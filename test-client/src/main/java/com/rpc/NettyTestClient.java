package com.rpc;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.netty.client.NettyClient;


public class NettyTestClient {

    public static void main(String[] args) {

        NettyClient client = new NettyClient("127.0.0.1", 9999);
        /*
        注意这里 RpcClientProxy 通过传入不同的 Client（SocketClient、NettyClient）
        来切换客户端不同的发送方式。
         */
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(12, "This is a message");
        String res = helloService.hello(helloObject);
        System.out.println(res);
    }
}

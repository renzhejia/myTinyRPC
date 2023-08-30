package com.rpc;

import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.socket.client.SocketClient;


/**
 * 客户端方面，我们需要通过动态代理，生成代理对象，
 * 并且调用，动态代理会自动帮我们向服务端发送请求的：
 */
public class SocketTestClient {
    public static void main(String[] args) {

        SocketClient client = new SocketClient("127.0.0.1",9000);
        //下面两行代码实现生成了HelloService的代理对象
        RpcClientProxy proxy = new RpcClientProxy(client);
        HelloService helloService = proxy.getProxy(HelloService.class);

        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}

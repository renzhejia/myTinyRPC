package com.rpc.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC客户端动态代理
 *
 * @author ziyang
 */
public class RpcClientProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    private String host;
    private int port;

    public RpcClientProxy(String host, int port) {
        this.port = port;
        this.host = host;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
         /*
                接口就是一中特殊的类，它的目的是把实现类规范化，
                所以在编译的过程中只要是.java文件都会被编译成.class文件，不管是接口还是实现类
                所以Class<?>既可以表示普通的类的反射，也可以表示接口的反射
                 */
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                /*
                接口就是一中特殊的类，它的目的是把实现类规范化，
                所以在编译的过程中只要是.java文件都会被编译成.class文件，不管是接口还是实现类
                所以Class<?>既可以表示普通的类的反射，也可以表示接口的反射
                 */
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse) rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
}

package com.rpc;

import com.rpc.entity.RpcRequest;
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
    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client=client;
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

    //================================初版，只能实现SocketClient不能实现NettyClient，下面的方法是对该方法的改进
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        RpcRequest rpcRequest = RpcRequest.builder()
//                /*
//                接口就是一中特殊的类，它的目的是把实现类规范化，
//                所以在编译的过程中只要是.java文件都会被编译成.class文件，不管是接口还是实现类
//                所以Class<?>既可以表示普通的类的反射，也可以表示接口的反射
//                 */
//                .interfaceName(method.getDeclaringClass().getName())
//                .methodName(method.getName())
//                .parameters(args)
//                .paramTypes(method.getParameterTypes())
//                .build();
//        SocketClient socketClient = new SocketClient(host,port);
//        return ((RpcResponse) socketClient.sendRequest(rpcRequest)).getData();
//    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes());
        return client.sendRequest(rpcRequest);
    }
}

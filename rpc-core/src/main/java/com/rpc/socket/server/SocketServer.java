package com.rpc.socket.server;

import com.rpc.handler.RequestHandler;
import com.rpc.RpcServer;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.factory.ThreadPoolFactory;
import com.rpc.provider.ServiceProvider;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.NacosServiceRegistry;
import com.rpc.registry.ServiceRegistry;
import com.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 远程方法调用的提供者（服务端）
 *
 * @author ziyang
 */
public class SocketServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private final ExecutorService threadPool;;
    private final String host;
    private final int port;
    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();

    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
    }


    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        if(serializer==null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvider(service,serviceClass);
        serviceRegistry.register(serviceClass.getCanonicalName(),new InetSocketAddress(host, port));
        start();
    }

    /*
    在创建 RpcServer 时需要传入一个已经注册好服务的 ServiceRegistry，
    而原来的 register 方法也被改成了 start 方法，因为服务的注册已经不由 RpcServer 处理了，它只需要启动就行了。
     */
    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器启动……");
            Socket socket;
            while((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }



}

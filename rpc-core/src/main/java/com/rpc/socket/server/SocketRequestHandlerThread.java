package com.rpc.socket.server;

import com.rpc.handler.RequestHandler;
import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.provider.ServiceProvider;
import com.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketRequestHandlerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketRequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private CommonSerializer serializer;

    public SocketRequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    /*
    而在每一个请求处理线程（RequestHandlerThread）中也就需要传入 ServiceRegistry 了，
    这里把处理线程和处理逻辑分成了两个类：RequestHandlerThread 只是一个线程，从ServiceRegistry
    获取到提供服务的对象后，就会把 RpcRequest 和服务对象直接交给 RequestHandler 去处理，反射等过程被放到了 RequestHandler 里。
     */
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = requestHandler.handler(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生:", e);
        }
    }
}

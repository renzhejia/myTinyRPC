package com.rpc;

import com.rpc.entity.RpcRequest;

public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);
}

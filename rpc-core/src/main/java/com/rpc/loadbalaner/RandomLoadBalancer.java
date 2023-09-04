package com.rpc.loadbalaner;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;


public class RandomLoadBalancer implements LoadBalancer {

    //随机访问
    @Override
    public Instance select(List<Instance> instances) {
        return instances.get(new Random().nextInt(instances.size()));
    }
}

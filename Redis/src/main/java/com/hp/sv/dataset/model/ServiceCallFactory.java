package com.hp.sv.dataset.model;

import com.hp.sv.Repository;

import java.util.Iterator;

public class ServiceCallFactory {

    private final String serviceCallCounterName = Repository.serviceCallName + ":" + Repository.counter;
    private final String leafCounterName = Repository.leafName + ":" + Repository.counter;
    private final Repository repository;
    private final String content = "valuevaluevaluevaluevalue";

    public ServiceCallFactory(Repository repository) {
        this.repository = repository;
    }

    public Iterable<ServiceCall> GenerateCalls(final int serviceCallsCount, final int leavesCount) {
        return new Iterable<ServiceCall>() {
            @Override
            public Iterator<ServiceCall> iterator() {
                final int size = serviceCallsCount;

                return new Iterator<ServiceCall>() {
                    int count = 0;

                    @Override
                    public boolean hasNext() {
                        return count < size;
                    }

                    @Override
                    public ServiceCall next() {
                        count++;
                        ServiceCall serviceCall = new ServiceCall(repository.Inc(serviceCallCounterName));
                        AddLeaves(serviceCall, leavesCount);
                        return serviceCall;
                    }

                    @Override
                    public void remove() {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
            }
        };
    }

    private static int id = 0;
    private void AddLeaves(ServiceCall serviceCall, int count) {
        for (int i = 0; i < count; i++) {
//            long id = repository.Inc(leafCounterName);
            long newId = id++;
            Leaf leaf = new Leaf(newId, Leaf.Type.First, new String(content));
            serviceCall.Add(leaf);
        }
    }
}

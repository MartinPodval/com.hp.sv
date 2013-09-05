package com.hp.sv.runtime.reports.inmemory.akka.client.model;

public class RuntimeReport extends AbstractVirtualServiceActor {
    private int count;

    public RuntimeReport(int virtualServiceId, int count) {
        super(virtualServiceId);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

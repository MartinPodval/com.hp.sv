package com.hp.sv.runtime.reports.inmemory.akka.client.model;

import org.apache.commons.lang3.Validate;

class AbstractVirtualServiceActor {
    private int virtualServiceId;

    protected AbstractVirtualServiceActor(int virtualServiceId) {
        Validate.isTrue(virtualServiceId > 0);
        this.virtualServiceId = virtualServiceId;
    }

    public int getVirtualServiceId() {
        return virtualServiceId;
    }
}

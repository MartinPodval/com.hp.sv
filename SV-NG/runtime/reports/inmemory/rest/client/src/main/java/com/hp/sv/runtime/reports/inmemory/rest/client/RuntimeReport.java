package com.hp.sv.runtime.reports.inmemory.rest.client;

import org.apache.commons.lang3.Validate;

public class RuntimeReport {

    private int virtualServiceId;
    private int count;

    public RuntimeReport(int virtualServiceId, int count) {
        Validate.isTrue(virtualServiceId > 0);
        Validate.isTrue(count > 0);

        this.virtualServiceId = virtualServiceId;
        this.count = count;
    }

    public int getVirtualServiceId() {
        return virtualServiceId;
    }

    public int getCount() {
        return count;
    }
}

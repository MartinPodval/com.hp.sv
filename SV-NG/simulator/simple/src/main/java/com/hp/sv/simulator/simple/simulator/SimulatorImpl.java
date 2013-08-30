package com.hp.sv.simulator.simple.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.lang3.Validate;

public class SimulatorImpl implements Simulator {

    private RuntimeReportsClient runtimeReportsClient;

    public SimulatorImpl(RuntimeReportsClient runtimeReportsClient) {
        Validate.notNull(runtimeReportsClient);
        this.runtimeReportsClient = runtimeReportsClient;
    }

    public Object getResponse(Object request, int serviceId) {
        Validate.notNull(request);
        runtimeReportsClient.increaseServiceUsageCount(serviceId);
        return null;
    }
}

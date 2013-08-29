package com.hp.sv.simulator.simple.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsService;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.lang3.Validate;

public class SimulatorImpl implements Simulator {

    private RuntimeReportsService runtimeReportsService;

    public SimulatorImpl(RuntimeReportsService runtimeReportsService) {
        Validate.notNull(runtimeReportsService);
        this.runtimeReportsService = runtimeReportsService;
    }

    public Object getResponse(Object request, int serviceId) {
        Validate.notNull(request);
        runtimeReportsService.increaseServiceUsageCount(serviceId);
        return null;
    }
}

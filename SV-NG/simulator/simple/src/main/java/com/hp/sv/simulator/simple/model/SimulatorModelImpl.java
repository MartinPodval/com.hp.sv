package com.hp.sv.simulator.simple.model;

import com.hp.sv.simulator.api.model.SimulatorModel;
import org.apache.commons.lang3.Validate;

public class SimulatorModelImpl implements SimulatorModel {

    private int id;

    public SimulatorModelImpl(int id) {
        Validate.isTrue(id > 0);
        this.id = id;
    }

    public int GetId() {
        return id;
    }

    public void SetId(int id) {
        Validate.isTrue(id > 0);
        this.id = id;
    }
}

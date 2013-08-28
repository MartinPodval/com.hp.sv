package com.hp.sv.simulator.simple.model;

import com.hp.sv.simulator.api.model.Dataset;
import org.apache.commons.lang3.Validate;

public class DatasetImpl implements Dataset {

    private int id;

    public DatasetImpl(int id) {
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

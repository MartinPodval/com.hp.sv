package com.hp.sv.simulator.simple.repository;

import com.hp.sv.simulator.api.model.Dataset;
import com.hp.sv.simulator.api.repository.SimulationRepository;
import org.apache.commons.lang3.Validate;

public class SimulationRepositoryImpl implements SimulationRepository {

    public Dataset GetById(int id) {
        Validate.isTrue(id > 0);
        return null;
    }
}

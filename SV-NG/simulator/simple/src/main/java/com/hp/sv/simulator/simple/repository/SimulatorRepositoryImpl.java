package com.hp.sv.simulator.simple.repository;

import com.hp.sv.simulator.api.model.SimulatorModel;
import com.hp.sv.simulator.api.repository.SimulatorRepository;
import org.apache.commons.lang3.Validate;

public class SimulatorRepositoryImpl implements SimulatorRepository {

    public SimulatorModel GetById(int id) {
        Validate.isTrue(id > 0);
        return null;
    }
}

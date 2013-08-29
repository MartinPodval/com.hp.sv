package com.hp.sv.simulator.api.repository;

import com.hp.sv.simulator.api.model.SimulatorModel;

public interface SimulatorRepository {

    public SimulatorModel GetById(int id);
}

package com.hp.sv.simulator.api.repository;

import com.hp.sv.simulator.api.model.Dataset;

public interface SimulationRepository {

    public Dataset GetById(int id);
}

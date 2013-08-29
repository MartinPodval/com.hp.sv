package com.hp.sv.simulator.simple.test.unit.model;

import com.hp.sv.simulator.simple.model.SimulatorModelImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimulationModelImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void Ctor_0_Id_Throws() {
        new SimulatorModelImpl(0);
    }

}

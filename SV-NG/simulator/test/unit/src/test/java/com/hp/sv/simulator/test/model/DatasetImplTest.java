package com.hp.sv.simulator.test.model;

import com.hp.sv.simulator.simple.model.DatasetImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DatasetImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void Ctor_0_Id_Throws() {
        new DatasetImpl(0);
    }

}

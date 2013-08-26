package com.hp.sv.persistence;

import com.hp.sv.persistence.api.ModelEntityRepository;
import com.hp.sv.persistence.impl.ModelEntityRepositoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ModelEntityRepositoryImplTest {

    protected ModelEntityRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new ModelEntityRepositoryImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void Throws_When_Not_Valid_Id() {
        repository.GetById(0);
    }
}

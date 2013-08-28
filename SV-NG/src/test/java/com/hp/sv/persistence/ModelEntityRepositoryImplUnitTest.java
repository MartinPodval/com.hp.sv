package com.hp.sv.persistence;

import com.hp.sv.repository.api.ModelEntityRepository;
import com.hp.sv.repository.impl.ModelEntityRepositoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(JUnit4.class)
public class ModelEntityRepositoryImplUnitTest {

    protected ModelEntityRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new ModelEntityRepositoryImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void Throws_When_Not_Valid_Id() {
        repository.GetById(0);
    }

    @Test
    public void Returns_Null_If_Id_Is_Valid() {
        assertThat(repository.GetById(23), is(nullValue()));
    }
}

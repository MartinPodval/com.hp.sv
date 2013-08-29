import com.hp.sv.simulator.api.repository.SimulationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulationRepositoryImplTest {

    @Autowired
    protected SimulationRepository simulationRepository;

    @Test
    public void GetById_Return_Found_Entity() {
        assertThat(simulationRepository.GetById(1), is(nullValue()));
    }
}

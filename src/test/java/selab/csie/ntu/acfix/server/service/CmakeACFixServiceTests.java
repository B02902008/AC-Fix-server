package selab.csie.ntu.acfix.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import selab.csie.ntu.acfix.server.model.FixingRecord;

class CmakeACFixServiceTests {

    private CmakeACFixService service;

    @BeforeEach
    void setup() {
        service = new CmakeACFixService(null, null, null);
    }

    /* Test generate record */
    @Test
    void testGenerateRecord() {
        String url = "https://github.com/User/Project";
        FixingRecord record = service.generateNewRecord(url);
        assertThat(record.getName()).isEqualTo("Project");
        assertThat(record.getLang()).isEqualTo("C++");
        assertThat(record.getTool()).isEqualTo("Cmake");
    }


}

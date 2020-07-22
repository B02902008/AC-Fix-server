package selab.csie.ntu.autofix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import selab.csie.ntu.autofix.server.model.FixingRecord;

class PipAutoFixServiceTests {

    private PipAutoFixService service;

    @BeforeEach
    void setup() {
        service = new PipAutoFixService(null, null);
    }

    /* Test generate record */
    @Test
    void testGenerateRecord() {
        String url = "https://pypi.org/project/Project";
        FixingRecord record = service.generateNewRecord(url);
        assertThat(record.getName()).isEqualTo("Project");
        assertThat(record.getLang()).isEqualTo("Python");
        assertThat(record.getTool()).isEqualTo("Pip");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex01() {
        assertThat(service.generateNewRecord("https://pypi.org/project/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex02() {
        assertThat(service.generateNewRecord("http://pypi.org/project/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex03() {
        assertThat(service.generateNewRecord("https://pypi.org/project/Project/").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern unmatched */
    @Test
    void testRegexUnmatched() {
        assertThat(catchThrowable(() -> service.generateNewRecord("https://selab.org/project/Project")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}

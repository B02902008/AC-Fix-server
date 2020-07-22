package selab.csie.ntu.autofix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import selab.csie.ntu.autofix.server.model.FixingRecord;

class GradleAutoFixServiceTests {

    private GradleAutoFixService service;

    @BeforeEach
    void setup() {
        service = new GradleAutoFixService(null, null);
    }

    /* Test generate record */
    @Test
    void testGenerateRecord() {
        String url = "https://github.com/User/Project";
        FixingRecord record = service.generateNewRecord(url);
        assertThat(record.getName()).isEqualTo("Project");
        assertThat(record.getLang()).isEqualTo("Java");
        assertThat(record.getTool()).isEqualTo("Gradle");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex01() {
        assertThat(service.generateNewRecord("https://github.com/User/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex02() {
        assertThat(service.generateNewRecord("http://github.com/User/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex03() {
        assertThat(service.generateNewRecord("https://gitlab.com/User/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex04() {
        assertThat(service.generateNewRecord("https://gitlab.com/Group/User/Project").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex05() {
        assertThat(service.generateNewRecord("https://github.com/User/Project.git").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern matched */
    @Test
    void testRegex06() {
        assertThat(service.generateNewRecord("https://github.com/User/Project.git.git").getName()).isEqualTo("Project");
    }

    /* Test Regex pattern unmatched */
    @Test
    void testRegexUnmatched() {
        assertThat(catchThrowable(() -> service.generateNewRecord("https://selab.com/User/Project.git")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }


}

package selab.csie.ntu.acfix.server.config;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GlobalConfigTests {

    @Autowired
    private GlobalConfig config;

    @Test
    void testConfiguredPropertyAppGlobalDockerMode() {
        assertThat(config.isDockerMode()).isTrue();
    }

    @Test
    void testConfiguredPropertyAppGlobalVolumeName() {
        assertThat(config.getVolumeName()).isEqualTo("ac-fix_ServerVolume");
    }

    @Test
    void testConfiguredPropertyAppGlobalVolumePath() {
        assertThat(config.getVolumePath()).isEqualTo("./data");
    }

}

package selab.csie.ntu.acfix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.util.FileSystemUtils;
import selab.csie.ntu.acfix.server.config.GlobalConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

class HistoryServiceTests {

    private HistoryService service;

    @BeforeAll
    static void mockData() {
        File dataDir = new File("./data");
        if (!dataDir.exists() && !dataDir.mkdir())
            throw new RuntimeException("Failed to create path: ./data");
        try {
            File src = new File(Objects.requireNonNull(HistoryServiceTests.class.getClassLoader().getResource("0")).getFile());
            File dst = new File("./data/0");
            FileSystemUtils.copyRecursively(src, dst);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy mock file to path: ./data/0");
        }
    }

    @BeforeEach
    void setup() {
        WebSocketService socketService = Mockito.mock(WebSocketService.class);
        GlobalConfig config = Mockito.mock(GlobalConfig.class);
        BDDMockito.given(socketService.socketAlive(anyString())).willReturn(false);
        BDDMockito.given(config.getVolumePath()).willReturn("./data");
        service = new HistoryService(socketService, config);
    }

    /* Test retrieve product not exists */
    @Test
    void testRetrieveProductNotExists() {
        assertThat(catchThrowable(() -> service.retrieveFixingProduct(-1)))
                .isExactlyInstanceOf(FileNotFoundException.class);
    }

    /* Test retrieve product */
    @Test
    void testRetrieveProduct() throws Exception {
        assertThat(service.retrieveFixingProduct(0)).isEqualTo("./data/0/0.tar.gz");
    }

    /* Test invoke log stream with sockID not alive */
    @Test
    void testInvokeLogStreamNotAlive() {
        assertThat(catchThrowable(() -> service.invokeLogStream(0, "")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @AfterAll
    static void mockRemove() {
        FileSystemUtils.deleteRecursively(new File("./data/0"));
    }

}

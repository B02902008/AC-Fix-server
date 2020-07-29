package selab.csie.ntu.acfix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import selab.csie.ntu.acfix.server.model.FixingRecord;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.util.FixingRecordBuilder;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

class ACFixServiceTests {

    private FixingRecordService recordService;
    private ACFixService service;
    private ThreadPoolExecutor mockPool;

    @BeforeEach
    void setup() {
        recordService = Mockito.mock(FixingRecordService.class);
        BDDMockito.given(recordService.addNewRecord(any(FixingRecord.class))).willReturn(new FixingRecordBuilder().setId(1).build());
        service = new GradleACFixService(recordService, null, null);
        mockPool = Mockito.mock(ThreadPoolExecutor.class);
    }

    /* Test invoke autofix */
    @Test
    void testInvokeAutoFix() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(99);
        BDDMockito.willDoNothing().given(mockPool).execute(any(Runnable.class));
        service.setPool(mockPool);
        assertThat(service.invokeACFix(new ACFixInvokeMessage(), new FixingRecord())).isEqualTo(1);
    }

    /* Test invoke autofix rejected for at least 100 active thread */
    @Test
    void testInvokeAutoFixRejected100() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(100);
        service.setPool(mockPool);
        assertThat(catchThrowable(() -> service.invokeACFix(new ACFixInvokeMessage(), new FixingRecord())))
                .isExactlyInstanceOf(RejectedExecutionException.class);
    }

    /* Test invoke autofix rejected by pool */
    @Test
    void testInvokeAutoFixPoolReject() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(99);
        BDDMockito.willThrow(new RejectedExecutionException()).given(mockPool).execute(any(Runnable.class));
        service.setPool(mockPool);
        assertThat(catchThrowable(() -> service.invokeACFix(new ACFixInvokeMessage(), new FixingRecord())))
                .isExactlyInstanceOf(RejectedExecutionException.class);
        Mockito.verify(recordService, Mockito.times(1)).removeRecord(eq(1));
    }

    /* Test get loading */
    @Test
    void testGetLoading() {
        Map<String, Integer> map = service.getLoading();
        assertThat(map.getOrDefault("core", -1)).isEqualTo(100);
        assertThat(map.getOrDefault("load", -1)).isZero();
    }

}

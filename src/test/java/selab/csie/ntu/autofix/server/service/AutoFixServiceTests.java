package selab.csie.ntu.autofix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.util.FixingRecordBuilder;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

class AutoFixServiceTests {

    private FixingRecordService recordService;
    private AutoFixService service;
    private ThreadPoolExecutor mockPool;

    @BeforeEach
    void setup() {
        recordService = Mockito.mock(FixingRecordService.class);
        BDDMockito.given(recordService.addNewRecord(any(FixingRecord.class))).willReturn(new FixingRecordBuilder().setId(1).build());
        service = new GradleAutoFixService(recordService, null);
        mockPool = Mockito.mock(ThreadPoolExecutor.class);
    }

    /* Test invoke autofix */
    @Test
    void testInvokeAutoFix() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(99);
        BDDMockito.willDoNothing().given(mockPool).execute(any(Runnable.class));
        service.setPool(mockPool);
        assertThat(service.invokeAutoFix(new AutoFixInvokeMessage(), new FixingRecord())).isEqualTo(1);
    }

    /* Test invoke autofix rejected for at least 100 active thread */
    @Test
    void testInvokeAutoFixRejected100() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(100);
        service.setPool(mockPool);
        assertThat(catchThrowable(() -> service.invokeAutoFix(new AutoFixInvokeMessage(), new FixingRecord())))
                .isExactlyInstanceOf(RejectedExecutionException.class);
    }

    /* Test invoke autofix rejected by pool */
    @Test
    void testInvokeAutoFixPoolReject() {
        BDDMockito.given(mockPool.getActiveCount()).willReturn(99);
        BDDMockito.willThrow(new RejectedExecutionException()).given(mockPool).execute(any(Runnable.class));
        service.setPool(mockPool);
        assertThat(catchThrowable(() -> service.invokeAutoFix(new AutoFixInvokeMessage(), new FixingRecord())))
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

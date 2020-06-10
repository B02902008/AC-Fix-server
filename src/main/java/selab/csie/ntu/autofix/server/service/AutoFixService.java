package selab.csie.ntu.autofix.server.service;

import selab.csie.ntu.autofix.server.exception.ServiceUnavailableException;
import selab.csie.ntu.autofix.server.model.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.thread.AutoFixThreadWork;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AutoFixService {

    FixingRecordService fixingRecordService;
    WebSocketService webSocketService;
    ThreadPoolExecutor pool;
    String dockerImage;

    static final Integer CORE_POOL_SIZE = 100;
    static final Integer MAX_POOL_SIZE = 110;
    static final Long ALIVE_TIME = 0L;
    static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;


    public abstract FixingRecord generateNewRecord(String url);

    public void invokeAutoFix(AutoFixInvokeMessage message, FixingRecord record) {
        if ( pool.getActiveCount() >= CORE_POOL_SIZE )
            throw new ServiceUnavailableException("Auto-Fix service reached system load limit, retry later.");
        Integer id = fixingRecordService.addNewRecord(record).getId();
        try {
            pool.execute(new AutoFixThreadWork(id, message, fixingRecordService, webSocketService));
        } catch (RejectedExecutionException e) {
            fixingRecordService.removeRecord(id);
            throw new ServiceUnavailableException("Auto-Fix service reached system load limit, retry later.");
        }
    }

}

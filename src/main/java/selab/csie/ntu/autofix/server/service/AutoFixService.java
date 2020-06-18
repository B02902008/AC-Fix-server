package selab.csie.ntu.autofix.server.service;

import selab.csie.ntu.autofix.server.service.exception.ServiceUnavailableException;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.thread.AutoFixThreadWork;

import java.util.HashMap;
import java.util.Map;
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
        printLoading();
        Integer id = fixingRecordService.addNewRecord(record).getId();
        try {
            pool.execute(new AutoFixThreadWork(id, message, fixingRecordService, webSocketService));
        } catch (RejectedExecutionException e) {
            fixingRecordService.removeRecord(id);
            throw new ServiceUnavailableException("Auto-Fix service reached system load limit, retry later.");
        }
        printLoading();
    }

    public Map<String, Integer> getLoading() {
        Map<String, Integer> map = new HashMap<>();
        map.put("load", pool.getActiveCount());
        map.put("core", pool.getCorePoolSize());
        return map;
    }

    public void printLoading() {
        System.out.println(String.format("Pool %d current loading: %d", pool.hashCode(), pool.getActiveCount()));
    }

}

package selab.csie.ntu.autofix.server.service;

import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.thread.AutoFixThreadWork;
import selab.csie.ntu.autofix.server.service.thread.CustomThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AutoFixService {

    private FixingRecordService fixingRecordService;
    private WebSocketService webSocketService;
    private ThreadPoolExecutor pool;
    String dockerImage;

    protected AutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        this.fixingRecordService = fixingRecordService;
        this.webSocketService = webSocketService;
        this.pool = new CustomThreadPool(100, 110, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    /* For unit test */
    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public abstract FixingRecord generateNewRecord(String url);

    public Integer invokeAutoFix(AutoFixInvokeMessage message, FixingRecord record) {
        if ( pool.getActiveCount() >= 100 )
            throw new RejectedExecutionException();
        Integer id = fixingRecordService.addNewRecord(record).getId();
        try {
            pool.execute(new AutoFixThreadWork(id, dockerImage, message, Runtime.getRuntime(), fixingRecordService, webSocketService));
        } catch (RejectedExecutionException e) {
            fixingRecordService.removeRecord(id);
            throw new RejectedExecutionException();
        }
        return id;
    }

    public Map<String, Integer> getLoading() {
        Map<String, Integer> map = new HashMap<>();
        map.put("load", pool.getActiveCount());
        map.put("core", pool.getCorePoolSize());
        return map;
    }

}

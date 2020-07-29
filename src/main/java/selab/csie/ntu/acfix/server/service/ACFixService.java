package selab.csie.ntu.acfix.server.service;

import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.model.FixingRecord;
import selab.csie.ntu.acfix.server.service.thread.AutoFixThreadWork;
import selab.csie.ntu.acfix.server.service.thread.CustomThreadPool;
import selab.csie.ntu.acfix.server.service.thread.DockerClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ACFixService {

    private FixingRecordService recordService;
    private WebSocketService socketService;
    private GlobalConfig config;
    private ThreadPoolExecutor pool;
    String dockerImage;

    protected ACFixService(FixingRecordService recordService, WebSocketService socketService, GlobalConfig config) {
        this.recordService = recordService;
        this.socketService = socketService;
        this.config = config;
        this.pool = new CustomThreadPool(100, 110, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    /* For unit test */
    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public abstract FixingRecord generateNewRecord(String url);

    public Integer invokeACFix(ACFixInvokeMessage message, FixingRecord record) {
        if ( pool.getActiveCount() >= 100 )
            throw new RejectedExecutionException();
        Integer id = recordService.addNewRecord(record).getId();
        try {
            pool.execute(new AutoFixThreadWork(id, dockerImage, message,
                    new DockerClient(Runtime.getRuntime()), recordService, socketService, config));
        } catch (RejectedExecutionException e) {
            recordService.removeRecord(id);
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

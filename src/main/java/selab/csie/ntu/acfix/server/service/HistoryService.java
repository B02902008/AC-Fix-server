package selab.csie.ntu.acfix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.service.thread.LogStreamThreadWork;

import java.io.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class HistoryService {

    private WebSocketService service;
    private GlobalConfig config;
    private ThreadPoolExecutor pool;

    @Autowired
    public HistoryService(WebSocketService service, GlobalConfig config) {
        this.service = service;
        this.config = config;
        this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    public String retrieveFixingProduct(Integer id) throws FileNotFoundException {
        File dir = new File(config.getVolumePath(), id.toString());
        FilenameFilter filter = (file, s) -> s.endsWith(".tar.gz");
        String[] products = dir.list(filter);
        if (products == null || products.length == 0)
            throw new FileNotFoundException();
        return new File(dir, products[0]).getPath();
    }

    public void invokeLogStream(Integer id, String socketID) {
        if ( !service.socketAlive(socketID) )
            throw new IllegalArgumentException();
        pool.execute(new LogStreamThreadWork(id, socketID, service, config));
    }

}

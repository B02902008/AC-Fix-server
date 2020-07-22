package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.service.thread.LogStreamThreadWork;

import java.io.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class HistoryService {

    private WebSocketService service;
    private ThreadPoolExecutor pool;

    private static final String AUTOFIX_RESULT_DIRECTORY = "./data";

    @Autowired
    public HistoryService(WebSocketService service) {
        this.service = service;
        this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    public String retrieveFixingProduct(Integer id) throws FileNotFoundException {
        File directory = new File(String.format("%s/%d/", AUTOFIX_RESULT_DIRECTORY, id));
        FilenameFilter filter = (file, s) -> s.endsWith(".tar.gz");
        String[] products = directory.list(filter);
        if (products == null || products.length == 0)
            throw new FileNotFoundException();
        return String.format("%s/%d/%s", AUTOFIX_RESULT_DIRECTORY, id, products[0]);
    }

    public void invokeLogStream(Integer id, String socketID) {
        if ( !service.socketAlive(socketID) )
            throw new IllegalArgumentException();
        pool.execute(new LogStreamThreadWork(id, socketID, service));
    }

}

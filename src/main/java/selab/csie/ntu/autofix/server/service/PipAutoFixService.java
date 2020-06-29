package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.thread.CustomThreadPool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PipAutoFixService extends AutoFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://pypi.org/project/([-.\\w]+)/?");

    @Autowired
    public PipAutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        this.fixingRecordService = fixingRecordService;
        this.webSocketService = webSocketService;
        this.pool = new CustomThreadPool(CORE_POOL_SIZE, MAX_POOL_SIZE, ALIVE_TIME, TIME_UNIT,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
        this.dockerImage = "autofix/pip-autofix:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) throws IllegalArgumentException {
        Matcher matcher = URL_PATTERN.matcher(url);
        if ( !matcher.matches() )
            throw new IllegalArgumentException();
        String name = matcher.group(1);
        return new FixingRecord(name, "Python", "Pip");
    }

}

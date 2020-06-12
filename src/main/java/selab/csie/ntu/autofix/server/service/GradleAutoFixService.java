package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.service.exception.BadRequestException;
import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GradleAutoFixService extends AutoFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://git(hub|lab).com/([-.\\w]+/)+([-.\\w]+)");

    @Autowired
    public GradleAutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        this.fixingRecordService = fixingRecordService;
        this.webSocketService = webSocketService;
        this.pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, ALIVE_TIME, TIME_UNIT,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
        this.dockerImage = "autofix/gradle-autofix:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if ( !matcher.matches() )
            throw new BadRequestException("Requires a valid URL.");
        String name = matcher.group(3);
        while ( name.endsWith(".git") )
            name = name.substring(0, name.length() - 4);
        return new FixingRecord(name, "Java", "Gradle");
    }

}

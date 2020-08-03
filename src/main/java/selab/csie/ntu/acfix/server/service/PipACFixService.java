package selab.csie.ntu.acfix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.FixingRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PipACFixService extends ACFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://pypi\\.org/project/([-.\\w]+)/?");

    @Autowired
    public PipACFixService(FixingRecordService recordService, WebSocketService socketService, GlobalConfig config) {
        super(recordService, socketService, config);
        this.dockerImage = "acfix/acfix-service-pip:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) {
        return new FixingRecord(extractProjectNameFromURL(url), "Python", "Pip");
    }

    private static String extractProjectNameFromURL(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if ( !matcher.matches() )
            throw new IllegalArgumentException();
        return matcher.group(1);
    }

}

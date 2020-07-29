package selab.csie.ntu.acfix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.FixingRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GradleACFixService extends ACFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://git(?:hub|lab)\\.com/[-.\\w/]+/([-.\\w]+)");

    @Autowired
    public GradleACFixService(FixingRecordService recordService, WebSocketService socketService, GlobalConfig config) {
        super(recordService, socketService, config);
        this.dockerImage = "ac-fix/ac-fix-service-gradle:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) {
        return new FixingRecord(extractProjectNameFromURL(url), "Java", "Gradle");
    }

    static String extractProjectNameFromURL(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if ( !matcher.matches() )
            throw new IllegalArgumentException();
        String name = matcher.group(1);
        while ( name.endsWith(".git") )
            name = name.substring(0, name.length() - 4);
        return name;
    }

}

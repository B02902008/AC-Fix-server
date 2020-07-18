package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GradleAutoFixService extends AutoFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://git(?:hub|lab)\\.com/[-.\\w/]+/([-.\\w]+)");

    @Autowired
    public GradleAutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        super(fixingRecordService, webSocketService);
        this.dockerImage = "autofix/gradle-autofix:1.0";
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

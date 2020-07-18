package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PipAutoFixService extends AutoFixService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://pypi\\.org/project/([-.\\w]+)/?");

    @Autowired
    public PipAutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        super(fixingRecordService, webSocketService);
        this.dockerImage = "autofix/pip-autofix:1.0";
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

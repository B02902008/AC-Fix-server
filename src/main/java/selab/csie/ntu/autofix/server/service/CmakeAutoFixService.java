package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.model.FixingRecord;

@Service
public class CmakeAutoFixService extends AutoFixService {

    @Autowired
    public CmakeAutoFixService(FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        super(fixingRecordService, webSocketService);
        this.dockerImage = "autofix/cmake-autofix:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) {
        return new FixingRecord(GradleAutoFixService.extractProjectNameFromURL(url), "C++", "Cmake");
    }

}

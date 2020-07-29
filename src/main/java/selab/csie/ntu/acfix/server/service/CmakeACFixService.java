package selab.csie.ntu.acfix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.FixingRecord;

@Service
public class CmakeACFixService extends ACFixService {

    @Autowired
    public CmakeACFixService(FixingRecordService recordService, WebSocketService socketService, GlobalConfig config) {
        super(recordService, socketService, config);
        this.dockerImage = "ac-fix/ac-fix-service-cmake:1.0";
    }

    @Override
    public FixingRecord generateNewRecord(String url) {
        return new FixingRecord(GradleACFixService.extractProjectNameFromURL(url), "C++", "Cmake");
    }

}

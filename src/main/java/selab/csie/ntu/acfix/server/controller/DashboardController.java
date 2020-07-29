package selab.csie.ntu.acfix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import selab.csie.ntu.acfix.server.model.FixingRecord;
import selab.csie.ntu.acfix.server.service.FixingRecordService;

@RestController
@CrossOrigin("*")
@RequestMapping("/dashboard")
public class DashboardController {

    private FixingRecordService service;

    @Autowired
    public DashboardController(FixingRecordService service) {
        this.service = service;
    }

    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<FixingRecord> currentQueue() {
        return service.getCurrentFixings();
    }

    @GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<FixingRecord> recentResult() {
        return service.getRecentResults();
    }

}

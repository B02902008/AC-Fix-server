package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.FixingRecordService;

@RestController
@CrossOrigin("*")
@RequestMapping("/dashboard")
public class DashboardController {

    private FixingRecordService fixingRecordService;

    @Autowired
    public DashboardController(FixingRecordService fixingRecordService) {
        this.fixingRecordService = fixingRecordService;
    }

    @GetMapping(value = "/current", produces = "application/json")
    public Iterable<FixingRecord> currentQueue() {
        return fixingRecordService.getCurrentFixings();
    }

    @GetMapping(value = "/recent", produces = "application/json")
    public Iterable<FixingRecord> recentResult() {
        return fixingRecordService.getRecentResults();
    }

}

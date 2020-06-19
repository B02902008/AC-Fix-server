package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.CmakeAutoFixService;
import selab.csie.ntu.autofix.server.service.FixingRecordService;
import selab.csie.ntu.autofix.server.service.GradleAutoFixService;
import selab.csie.ntu.autofix.server.service.PipAutoFixService;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/debug")
public class DebugController {

    private FixingRecordService service;
    private CmakeAutoFixService cmakeAutoFixService;
    private GradleAutoFixService gradleAutoFixService;
    private PipAutoFixService pipAutoFixService;

    @Autowired
    public DebugController(
            FixingRecordService service,
            CmakeAutoFixService cmakeAutoFixService,
            GradleAutoFixService gradleAutoFixService,
            PipAutoFixService pipAutoFixService
    ) {
        this.service = service;
        this.cmakeAutoFixService = cmakeAutoFixService;
        this.gradleAutoFixService = gradleAutoFixService;
        this.pipAutoFixService = pipAutoFixService;
    }

    @PostMapping(value = "/insert", consumes = "application/json", produces = "application/json")
    public FixingRecord insertRecord(@RequestBody Map<String, String> map) {
        FixingRecord record = new FixingRecord(
                map.getOrDefault("name", "defaultName"),
                map.getOrDefault("lang", "defaultLang"),
                map.getOrDefault("tool", "defaultTool")
        );
        return service.addNewRecord(record);
    }

    @GetMapping(value = "/all", produces = "application/json")
    public Iterable<FixingRecord> getRecords() {
        return service.getFixingRecords();
    }

    @PatchMapping(value = "/update", consumes = "application/json", produces = "application/json")
    public FixingRecord updateRecord(@RequestBody Map<String, Object> map) {
        return service.updateRecord((Integer) map.get("id"), (Boolean) map.get("result"));
    }

    @DeleteMapping(value = "/remove/{id}", produces = "application/json")
    public void removeRecord(@PathVariable Integer id) {
        service.removeRecord(id);
    }

    @GetMapping(value = "/autofix/loading")
    public void printAutoFixLoading() {
        System.out.println("In Cmake:");
        cmakeAutoFixService.printLoading();
        System.out.println("In Gradle");
        gradleAutoFixService.printLoading();
        System.out.println("In Pip");
        pipAutoFixService.printLoading();
    }

}

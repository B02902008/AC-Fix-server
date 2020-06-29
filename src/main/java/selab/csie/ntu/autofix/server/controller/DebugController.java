package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.FixingRecordService;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/debug")
public class DebugController {

    private FixingRecordService service;

    @Autowired
    public DebugController(FixingRecordService service) {
        this.service = service;
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

}

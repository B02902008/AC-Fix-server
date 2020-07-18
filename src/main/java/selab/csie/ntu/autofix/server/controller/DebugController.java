package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertRecord(@RequestBody Map<String, String> map) {
        FixingRecord record = new FixingRecord(
                map.getOrDefault("name", "defaultName"),
                map.getOrDefault("lang", "defaultLang"),
                map.getOrDefault("tool", "defaultTool")
        );
        service.addNewRecord(record);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<FixingRecord> getRecords() {
        return service.getFixingRecords();
    }

    @PatchMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateRecord(@RequestBody Map<String, Object> map) {
        service.updateRecord((Integer) map.get("id"), (Boolean) map.get("result"));
    }

    @DeleteMapping(value = "/remove/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeRecord(@PathVariable Integer id) {
        service.removeRecord(id);
    }

}

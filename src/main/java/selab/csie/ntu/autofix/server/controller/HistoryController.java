package selab.csie.ntu.autofix.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.FixingRecordService;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/history")
public class HistoryController {

    private FixingRecordService service;

    @Autowired
    public HistoryController(FixingRecordService service) {
        this.service = service;
    }

    @GetMapping(value = "/", produces = "application/json")
    public Page<FixingRecord> allHistory() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, FixingRecordService.PER_PAGE, sort);
        return service.getFixingRecords(pageable);
    }

    @GetMapping(value = "/page/{page_num}", produces = "application/json")
    public Page<FixingRecord> allHistory(
            @PathVariable(name = "page_num") Integer page,
            @RequestParam(name = "sorting", required = false, defaultValue = "id") String sorting,
            @RequestParam(name = "direction", required = false, defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(
                direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                FixingRecord.hasField(sorting) ? new String[] { sorting, "id" } : new String[] { "id" }
        );
        Pageable pageable = PageRequest.of(page - 1, FixingRecordService.PER_PAGE, sort);
        return service.getFixingRecords(pageable);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public FixingRecord getHistory(@PathVariable(name = "id") Integer id) {
        return service.getFixingRecord(id);
    }

}

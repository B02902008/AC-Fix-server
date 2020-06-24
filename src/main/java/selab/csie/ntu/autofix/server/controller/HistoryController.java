package selab.csie.ntu.autofix.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.service.FixingRecordService;
import selab.csie.ntu.autofix.server.service.HistoryService;
import selab.csie.ntu.autofix.server.service.exception.BadRequestException;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/history")
public class HistoryController {

    private FixingRecordService fixingRecordService;
    private HistoryService historyService;

    @Autowired
    public HistoryController(FixingRecordService fixingRecordService, HistoryService historyService) {
        this.fixingRecordService = fixingRecordService;
        this.historyService = historyService;
    }

    @GetMapping(value = "/", produces = "application/json")
    public Page<FixingRecord> allHistory() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, FixingRecordService.PER_PAGE, sort);
        return fixingRecordService.getFixingRecords(pageable);
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
        return fixingRecordService.getFixingRecords(pageable);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public FixingRecord getHistory(@PathVariable(name = "id") Integer id) {
        return fixingRecordService.getFixingRecord(id);
    }

    @GetMapping(value = "/product/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFixingProduct(@PathVariable Integer id) {
        String filepath = this.historyService.retrieveFixingProduct(id);
        FileSystemResource resource = new FileSystemResource(filepath);
        return ResponseEntity.ok().body(resource);
    }

    @PostMapping(value = "/stream/{id}", consumes = "application/json")
    public void invokeLogStream(@PathVariable Integer id, @RequestBody AutoFixInvokeMessage message) {
        if ( message.getSocketID() == null )
            throw new BadRequestException("Requires target socket ID.");
        this.historyService.invokeLogStream(id, message.getSocketID());
    }

}

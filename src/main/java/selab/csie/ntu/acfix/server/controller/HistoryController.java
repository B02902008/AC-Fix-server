package selab.csie.ntu.acfix.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import selab.csie.ntu.acfix.server.model.FixingRecord;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.service.FixingRecordService;
import selab.csie.ntu.acfix.server.service.HistoryService;

import java.io.FileNotFoundException;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/history")
public class HistoryController {

    private FixingRecordService recordService;
    private HistoryService historyService;

    @Autowired
    public HistoryController(FixingRecordService recordService, HistoryService historyService) {
        this.recordService = recordService;
        this.historyService = historyService;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<FixingRecord> allHistory() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, FixingRecordService.PER_PAGE, sort);
        return recordService.getFixingRecords(pageable);
    }

    @GetMapping(value = "/page/{page_num}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<FixingRecord> allHistory(
            @PathVariable(name = "page_num") Integer page,
            @RequestParam(name = "sorting", required = false, defaultValue = "id") String sorting,
            @RequestParam(name = "direction", required = false, defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(
                direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                FixingRecord.hasField(sorting.toLowerCase()) && !sorting.equalsIgnoreCase("id") ?
                        new String[] { sorting, "id" } : new String[] { "id" }
        );
        Pageable pageable = PageRequest.of(page - 1, FixingRecordService.PER_PAGE, sort);
        return recordService.getFixingRecords(pageable);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FixingRecord getHistory(@PathVariable(name = "id") Integer id) {
        try {
            return recordService.getFixingRecord(id);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cannot find fixing record with id: %d.", id));
        }
    }

    @RequestMapping(value = "/product/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = { RequestMethod.GET, RequestMethod.HEAD })
    public ResponseEntity<Resource> getFixingProduct(@PathVariable Integer id) {
        String filepath;
        try {
            filepath = this.historyService.retrieveFixingProduct(id);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Fixing product for build index %d not found.", id));
        }
        String filename = filepath.substring(filepath.lastIndexOf('/') + 1);
        FileSystemResource resource = new FileSystemResource(filepath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @PostMapping(value = "/stream/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void invokeLogStream(@PathVariable Integer id, @RequestBody ACFixInvokeMessage message) {
        try {
            this.historyService.invokeLogStream(id, message.getSocketID());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requires a valid web socket ID.");
        } catch (RejectedExecutionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Log stream service reached system load limit, please retry later.");
        }
    }

}

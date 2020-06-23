package selab.csie.ntu.autofix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import selab.csie.ntu.autofix.server.service.exception.NotFoundException;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.repository.FixingRecordRepository;
import selab.csie.ntu.autofix.server.service.exception.ServiceUnavailableException;
import selab.csie.ntu.autofix.server.service.thread.LogStreamThreadWork;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class FixingRecordService {

    private WebSocketService webSocketService;
    private FixingRecordRepository repository;
    private ThreadPoolExecutor pool;
    public static final Integer PER_PAGE = 10;

    @Autowired
    public FixingRecordService(WebSocketService webSocketService, FixingRecordRepository repository) {
        this.webSocketService = webSocketService;
        this.repository = repository;
        this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    public Iterable<FixingRecord> getFixingRecords() {
        return repository.findAll();
    }

    public Page<FixingRecord> getFixingRecords(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public FixingRecord getFixingRecord(Integer id) {
        Optional<FixingRecord> record = repository.findById(id);
        if ( !record.isPresent() )
            throw new NotFoundException(String.format("Cannot find fixing record with id: %d.", id));
        return record.get();
    }

    public FixingRecord addNewRecord(FixingRecord record) {
        return repository.save(record);
    }

    public FixingRecord updateRecord(Integer id, Boolean result) {
        FixingRecord record = new FixingRecord();
        record.setId(id);
        record.setStat(result ? 1 : -1);
        record.setEnd(new Date());
        return repository.save(record);
    }

    public void removeRecord(Integer id) {
        repository.deleteById(id);
    }

    public Iterable<FixingRecord> getCurrentFixings() {
        return repository.findAllByStat(0, Sort.by(Sort.Direction.DESC, "id"));
    }

    public Iterable<FixingRecord> getRecentResults() {
        Sort sort = Sort.by(Sort.Direction.DESC, "end", "id");
        Pageable pageable = PageRequest.of(0, FixingRecordService.PER_PAGE, sort);
        return repository.findAllByStatNot(0, pageable).getContent();
    }

    public void invokeLogStream(Integer id, String socketID) {
        try {
            pool.execute(new LogStreamThreadWork(id, socketID, webSocketService));
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("Log stream service reached system load limit, retry later.");
        }
    }

}

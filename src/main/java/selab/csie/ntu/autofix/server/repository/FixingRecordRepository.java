package selab.csie.ntu.autofix.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.util.List;


@Repository
public interface FixingRecordRepository extends PagingAndSortingRepository<FixingRecord, Integer> {

    List<FixingRecord> findAllByStat(Integer stat, Sort sort);
    Page<FixingRecord> findAllByStatNot(Integer stat, Pageable pageable);

}

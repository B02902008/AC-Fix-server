package selab.csie.ntu.autofix.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FixingRecordServiceTests {

    @Autowired
    private FixingRecordService service;


    /* Test create */
    @Test
    @Order(2)
    void testCreate() {
        assertThat(service.addNewRecord(new FixingRecord()).getId()).isEqualTo(101);
    }

    /* Test retrieve single by id */
    @Test
    @Order(3)
    void testRetrieveById() throws FileNotFoundException {
        assertThat(service.getFixingRecord(101).getId()).isEqualTo(101);
    }

    /* Test retrieve single by id not found */
    @Test
    @Order(1)
    void testRetrieveNotFound() {
        assertThat(catchThrowable(() -> service.getFixingRecord(101))).isExactlyInstanceOf(FileNotFoundException.class);
    }

    /* Test retrieve sorted page */
    @Test
    @Order(6)
    void testRetrieveSortedPage() {
        PageRequest request = PageRequest.of(5, 15, Sort.Direction.ASC, "id");
        Page<FixingRecord> page = service.getFixingRecords(request);
        List<Integer> ids = page.stream().map(FixingRecord::getId).collect(Collectors.toList());
        assertThat(page.getTotalElements()).isEqualTo(100);
        assertThat(page.getTotalPages()).isEqualTo(7);
        assertThat(page.getSize()).isEqualTo(15);
        assertThat(page.getNumber()).isEqualTo(5);
        assertThat(ids).isSortedAccordingTo(Comparator.naturalOrder());
        assertThat(ids).isEqualTo(IntStream.range(76, 91).boxed().collect(Collectors.toList()));
    }

    /* Test update */
    @Test
    @Order(4)
    void testUpdate() throws FileNotFoundException {
        service.updateRecord(101, false);
        assertThat(service.getFixingRecord(101).getStat()).isEqualTo(-1);
    }

    /* Test delete */
    @Test
    @Order(5)
    void testDelete() {
        service.removeRecord(101);
        testRetrieveNotFound();
    }

    /* Test get current queue list */
    @Test
    void testCurrent() {
        List<FixingRecord> current = (List<FixingRecord>) service.getCurrentFixings();
        List<Integer> ids = current.stream().peek(record -> assertThat(record.getStat()).isEqualTo(0))
                .map(FixingRecord::getId)
                .collect(Collectors.toList());
        assertThat(ids).isSortedAccordingTo(Comparator.reverseOrder());
    }

    /* Test get recent result list */
    @Test
    void testRecent() {
        List<FixingRecord> recent = (List<FixingRecord>) service.getRecentResults();
        List<Date> ends = recent.stream().peek(record -> assertThat(record.getStat()).isNotEqualTo(0))
                .map(FixingRecord::getEnd)
                .collect(Collectors.toList());
        assertThat(ends).isSortedAccordingTo(Comparator.reverseOrder());
    }

}

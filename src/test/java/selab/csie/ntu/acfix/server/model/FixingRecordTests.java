package selab.csie.ntu.acfix.server.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FixingRecordTests {

    @Test
    void testInField() {
        String fieldInFixingRecord = "id";
        assertThat(FixingRecord.hasField(fieldInFixingRecord)).isTrue();
    }

    @Test
    void testNotInField() {
        String fieldNotInFixingRecord = "someField";
        assertThat(FixingRecord.hasField(fieldNotInFixingRecord)).isFalse();
    }

}

package selab.csie.ntu.autofix.server.util;

import selab.csie.ntu.autofix.server.model.FixingRecord;

import java.lang.reflect.Field;
import java.util.Date;

public class FixingRecordBuilder {

    private FixingRecord record;

    public FixingRecordBuilder() {
        this.record = new FixingRecord();
    }

    private void setFinalField(String key, Object value) {
        try {
            Field field = this.record.getClass().getDeclaredField(key);
            field.setAccessible(true);
            field.set(this.record, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
    }

    public FixingRecordBuilder setId(Integer id) {
        this.record.setId(id);
        return this;
    }

    public FixingRecordBuilder setStat(Integer stat) {
        this.record.setStat(stat);
        return this;
    }

    public FixingRecordBuilder setName(String name) {
        this.setFinalField("name", name);
        return this;
    }

    public FixingRecordBuilder setLang(String lang) {
        this.setFinalField("lang", lang);
        return this;
    }

    public FixingRecordBuilder setTool(String tool) {
        this.setFinalField("tool", tool);
        return this;
    }

    public FixingRecordBuilder setStart(Date start) {
        this.record.setStart(start);
        return this;
    }

    public FixingRecordBuilder setEnd(Date end) {
        this.record.setEnd(end);
        return this;
    }

    public FixingRecord build() {
        return this.record;
    }

}

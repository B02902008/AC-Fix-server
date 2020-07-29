package selab.csie.ntu.acfix.server.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "FixingRecord")
public class FixingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false, columnDefinition="TINYINT DEFAULT 0")
    private Integer stat;

    @Column(nullable = false, updatable = false)
    private final String name;

    @Column(nullable = false, updatable = false)
    private final String lang;

    @Column(nullable = false, updatable = false)
    private final String tool;

    @Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date start;

    @Column(insertable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date end;

    public FixingRecord() {
        this.name = "defaultName";
        this.lang = "defaultLang";
        this.tool = "defaultTool";
    }

    public FixingRecord(final String name, final String lang, final String tool) {
        this.name = name;
        this.lang = lang;
        this.tool = tool;
    }

    public static boolean hasField(String field) {
        try {
            FixingRecord.class.getDeclaredField(field);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

}

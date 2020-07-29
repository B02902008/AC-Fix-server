package selab.csie.ntu.acfix.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("app.global")
public class GlobalConfig {

    private boolean dockerMode;

    private String volumeName;

    private String volumePath;

}

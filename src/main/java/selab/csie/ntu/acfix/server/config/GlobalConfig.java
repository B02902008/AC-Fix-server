package selab.csie.ntu.acfix.server.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("app.global")
public class GlobalConfig {

    private boolean dockerMode;

    private String volumeName;

    private String volumePath;

}

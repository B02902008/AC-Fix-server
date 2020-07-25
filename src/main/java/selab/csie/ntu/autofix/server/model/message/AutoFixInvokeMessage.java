package selab.csie.ntu.autofix.server.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoFixInvokeMessage {

    String socketID;

    String url;

}

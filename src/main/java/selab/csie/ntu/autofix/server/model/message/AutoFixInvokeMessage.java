package selab.csie.ntu.autofix.server.model.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoFixInvokeMessage {

    String socketID;

    String url;

}

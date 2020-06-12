package selab.csie.ntu.autofix.server.model.message;

import lombok.Value;

@Value
public class AutoFixInvokeMessage {

    String socketID;

    String url;

}

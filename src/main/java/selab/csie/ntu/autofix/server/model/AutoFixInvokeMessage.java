package selab.csie.ntu.autofix.server.model;

import lombok.Value;

@Value
public class AutoFixInvokeMessage {

    String socketID;

    String url;

}

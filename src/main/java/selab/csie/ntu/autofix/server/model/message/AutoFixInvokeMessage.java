package selab.csie.ntu.autofix.server.model.message;

import lombok.Data;

@Data
public class AutoFixInvokeMessage {

    String socketID;

    String url;

}

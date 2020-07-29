package selab.csie.ntu.acfix.server.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ACFixInvokeMessage {

    String socketID;

    String url;

}

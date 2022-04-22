package cc.minetale.postman.payload;

import cc.minetale.postman.Postman;
import lombok.Getter;

@Getter
public class Payload {

    protected String origin = Postman.getPostman().getUnitId();

}

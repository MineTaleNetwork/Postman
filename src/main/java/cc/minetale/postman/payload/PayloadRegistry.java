package cc.minetale.postman.payload;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PayloadRegistry {

    @Getter private final Map<String, Class<? extends Payload>> payloads = new HashMap<>();

    public void registerPayload(Class<? extends Payload> payloadClass) {
        payloads.put(payloadClass.getSimpleName(), payloadClass);
    }

    @Nullable
    public Class<? extends Payload> getPayloadById(String id) {
        return payloads.get(id);
    }

}

package cc.minetale.postman;

import cc.minetale.postman.payload.ListenerRegistry;
import cc.minetale.postman.payload.Payload;
import cc.minetale.postman.payload.PayloadRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

@Getter
public class Postman {

    @Getter private static Postman postman;

    private final Gson gson;
    private final ListenerRegistry listenersRegistry;
    private final PayloadRegistry payloadsRegistry;

    private Channel channel;
    private Connection rabbitMqConnection;

    private final String networkId;
    private final String typeId;
    private final String unitId;

    private String queue;

    public static final String EXCHANGE = "postman";

    public Postman(Gson gson) {
        postman = this;

        this.gson = gson;

        listenersRegistry = new ListenerRegistry();
        payloadsRegistry = new PayloadRegistry();

        networkId = System.getProperty(EXCHANGE + "Network", "minetale");
        typeId = System.getProperty(EXCHANGE + "Type", "minecraft");
        unitId = System.getProperty(EXCHANGE + "Unit", StringUtil.generateId());

        setupRabbitMq();
    }

    public void setupRabbitMq() {
        try {
            var factory = new ConnectionFactory();

            factory.setHost(System.getProperty(EXCHANGE + "Host", "127.0.0.1"));
            factory.setPort(Integer.getInteger(EXCHANGE + "Port", 5672));

            factory.setUsername(System.getProperty(EXCHANGE + "Username", "guest"));
            factory.setPassword(System.getProperty(EXCHANGE + "Password", "guest"));

            factory.setVirtualHost(System.getProperty(EXCHANGE + "VirtualHost", "/"));

            rabbitMqConnection = factory.newConnection();
            channel = rabbitMqConnection.createChannel();

            channel.exchangeDeclare(EXCHANGE, "direct");

            queue = channel.queueDeclare().getQueue();

            try {
                channel.queueBind(queue, EXCHANGE, networkId + ":" + EXCHANGE + "-broadcast");
                channel.queueBind(queue, EXCHANGE, networkId + ":" + typeId);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            channel.basicConsume(queue, true, this::receive, consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void receive(String consumerTag, Delivery delivery) {
        var retrievedMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);

        var data = getDataFromBody(retrievedMessage);

        var payloadId = data[0];
        var payloadData = data[1];

        var payloadClass = payloadsRegistry.getPayloadById(payloadId);

        if (payloadClass == null) { return; }

        var payload = gson.fromJson(payloadData, payloadClass);

//        if(unitId.equals(payload.getOrigin())) { return; } // This allows for servers to receive their own payload.

        getListenersRegistry().callListeners(payload);
    }

    public void broadcast(Payload payload) {
        final var data = getTransmitReadyData(payload);
        send(EXCHANGE + "-broadcast", data);
    }

    public void sendTo(String typeId, Payload payload) {
        final var data = getTransmitReadyData(payload);
        send(typeId, data);
    }

    private void send(String routingKey, String message) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicPublish(EXCHANGE, networkId + ":" + routingKey, null, message.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getDataFromBody(String body) {
        var split = body.split("&");

        var payloadId = split[0];
        var payloadData = String.join("&", Arrays.copyOfRange(split, 1, split.length));

        return new String[]{ payloadId, payloadData };
    }

    public String getTransmitReadyData(Payload payload) {
        return payload.getClass().getSimpleName() + "&" + gson.toJson(payload);
    }

}

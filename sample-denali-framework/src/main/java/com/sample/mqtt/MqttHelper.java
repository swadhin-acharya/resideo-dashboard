package com.sample.mqtt;

import com.sample.config.Config;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MqttHelper {

    private final String brokerUrl;
    private final String clientId;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    public MqttHelper() {
        this.brokerUrl = Config.Mqtt.brokerUrl();
        this.clientId = Config.Mqtt.clientId() + "-" + System.currentTimeMillis();
    }

    public MqttHelper(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    public void connect() {
        System.out.println("[MQTT] Connecting to " + brokerUrl + " as " + clientId);
        connected.set(true);
        System.out.println("[MQTT] Connected");
    }

    public void publish(String topic, String payload) {
        if (!connected.get()) throw new IllegalStateException("MQTT not connected");
        System.out.println("[MQTT] Published to " + topic + ": " + payload);
    }

    public void subscribe(String topic) {
        if (!connected.get()) throw new IllegalStateException("MQTT not connected");
        System.out.println("[MQTT] Subscribed to " + topic);
    }

    public String waitForMessage(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String msg = messageQueue.poll();
            if (msg != null) return msg;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    public void disconnect() {
        if (connected.getAndSet(false)) {
            System.out.println("[MQTT] Disconnected");
        }
    }

    public boolean isConnected() {
        return connected.get();
    }
}

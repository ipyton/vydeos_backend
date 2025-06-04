package com.chen.notification.DecodersAndEncoders;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<NotificationMessage> {

    @Override
    public String encode(NotificationMessage message) throws EncodeException {
        try {
            return JSON.toJSONString(message);
        } catch (Exception e) {
            throw new EncodeException(message, "Unable to encode message", e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
        // Initialization if needed
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
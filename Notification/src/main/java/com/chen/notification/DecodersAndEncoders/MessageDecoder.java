package com.chen.notification.DecodersAndEncoders;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.Negotiation;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<Negotiation> {

    @Override
    public Negotiation decode(String message) throws DecodeException {
        try {
            return JSON.parseObject(message, Negotiation.class);
        } catch (Exception e) {
            throw new DecodeException(message, "Unable to decode message", e);
        }
    }

    @Override
    public boolean willDecode(String message) {
        try {
            JSON.parseObject(message);
            return true;
        } catch (Exception e) {
            return false;
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common.utils;

import com.google.protobuf.Parser;
import java.io.Serializable;

/**
 *
 * @author Shell
 */
public abstract class ProtoDeserializer<T extends Serializable> {

    private final Parser<T> parser;

    public ProtoDeserializer(Parser<T> parser) {
        this.parser = parser;
    }

    public T deserialize(final byte[] bytes) throws Exception {    
        T message = parser.parseFrom(bytes);
        validate(message);
        return message;
    }

    public abstract void validate(final T message) throws Exception;
}

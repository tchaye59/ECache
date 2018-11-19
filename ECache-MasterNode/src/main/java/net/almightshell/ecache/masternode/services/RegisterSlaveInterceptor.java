/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode.services;

import com.google.protobuf.ExperimentalApi;
import io.grpc.Attributes;
import io.grpc.ForwardingServerCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.net.SocketAddress;


/**
 *
 * @author Shell
 */
public final class RegisterSlaveInterceptor implements ServerInterceptor {

    public static String salveAdresse = null;

    @ExperimentalApi
    public static final Attributes.Key<SocketAddress> REMOTE_ADDR_KEY = Attributes.Key.create("io.grpc.RemoteAddr");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = serverCall.getMethodDescriptor().getFullMethodName();
        if (methodName != null && methodName.endsWith("registerSlave")) {

            String inetSocketString = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
            System.out.println("inetSocketString "+inetSocketString);
            RegisterSlaveInterceptor.salveAdresse = inetSocketString.replaceAll("/", "");
        }

        return next.startCall(
                new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                super.sendHeaders(responseHeaders);
            }
        },
                requestHeaders);
    }
}

/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.delegate;

import jakarta.json.JsonObject;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferRequestMessage;
import org.eclipse.edc.protocol.dsp.spi.dispatcher.DspHttpDispatcherDelegate;
import org.eclipse.edc.protocol.dsp.spi.serialization.JsonLdRemoteMessageSerializer;

import java.util.function.Function;

import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.BASE_PATH;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.TRANSFER_INITIAL_REQUEST;

public class TransferRequestDelegate extends DspHttpDispatcherDelegate<TransferRequestMessage, JsonObject> {

    public TransferRequestDelegate(JsonLdRemoteMessageSerializer serializer) {
        super(serializer);
    }

    @Override
    public Class<TransferRequestMessage> getMessageType() {
        return TransferRequestMessage.class;
    }

    @Override
    public Request buildRequest(TransferRequestMessage message) {
        return buildRequest(message, BASE_PATH + TRANSFER_INITIAL_REQUEST);
    }

    @Override
    public Function<Response, JsonObject> parseResponse() {
        return response -> null;
    }

}

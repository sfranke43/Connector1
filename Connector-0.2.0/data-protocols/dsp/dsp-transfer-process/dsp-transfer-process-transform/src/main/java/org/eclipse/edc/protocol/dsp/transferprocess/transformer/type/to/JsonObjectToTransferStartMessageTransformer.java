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

package org.eclipse.edc.protocol.dsp.transferprocess.transformer.type.to;

import jakarta.json.JsonObject;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferStartMessage;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;
import static org.eclipse.edc.protocol.dsp.type.DspTransferProcessPropertyAndTypeNames.DSPACE_PROPERTY_DATA_ADDRESS;
import static org.eclipse.edc.protocol.dsp.type.DspTransferProcessPropertyAndTypeNames.DSPACE_TYPE_TRANSFER_START_MESSAGE;

public class JsonObjectToTransferStartMessageTransformer extends AbstractJsonLdTransformer<JsonObject, TransferStartMessage> {

    public JsonObjectToTransferStartMessageTransformer() {
        super(JsonObject.class, TransferStartMessage.class);
    }

    @Override
    public @Nullable TransferStartMessage transform(@NotNull JsonObject messageObject, @NotNull TransformerContext context) {
        var transferStartMessageBuilder = TransferStartMessage.Builder.newInstance();

        if (!transformMandatoryString(messageObject.get(DSPACE_PROPERTY_PROCESS_ID), transferStartMessageBuilder::processId, context)) {
            context.problem()
                    .missingProperty()
                    .type(DSPACE_TYPE_TRANSFER_START_MESSAGE)
                    .property(DSPACE_PROPERTY_PROCESS_ID)
                    .report();
            return null;
        }

        var dataAddressObject = returnJsonObject(messageObject.get(DSPACE_PROPERTY_DATA_ADDRESS), context, DSPACE_PROPERTY_DATA_ADDRESS, false);
        if (dataAddressObject != null) {
            transferStartMessageBuilder.dataAddress(context.transform(dataAddressObject, DataAddress.class));
        }

        return transferStartMessageBuilder.build();
    }
}

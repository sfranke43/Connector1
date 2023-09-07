/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.connector.api.management.contractnegotiation.transform;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.api.management.contractnegotiation.model.NegotiationState;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.connector.api.management.contractnegotiation.model.NegotiationState.NEGOTIATION_STATE_STATE;
import static org.eclipse.edc.connector.api.management.contractnegotiation.model.NegotiationState.NEGOTIATION_STATE_TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;

public class JsonObjectFromNegotiationStateTransformer extends AbstractJsonLdTransformer<NegotiationState, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromNegotiationStateTransformer(JsonBuilderFactory jsonFactory) {
        super(NegotiationState.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull NegotiationState negotiationState, @NotNull TransformerContext context) {
        return jsonFactory.createObjectBuilder()
                .add(TYPE, NEGOTIATION_STATE_TYPE)
                .add(NEGOTIATION_STATE_STATE, negotiationState.state())
                .build();
    }
}

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

package org.eclipse.edc.protocol.dsp.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.protocol.dsp.spi.serialization.JsonLdRemoteMessageSerializer;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import static java.lang.String.format;
import static java.lang.String.join;

/**
 * Serializes {@link RemoteMessage}s to JSON-LD.
 */
public class JsonLdRemoteMessageSerializerImpl implements JsonLdRemoteMessageSerializer {

    private final TypeTransformerRegistry registry;
    private final ObjectMapper mapper;
    private final JsonLd jsonLdService;

    public JsonLdRemoteMessageSerializerImpl(TypeTransformerRegistry registry, ObjectMapper mapper, JsonLd jsonLdService) {
        this.registry = registry;
        this.mapper = mapper;
        this.jsonLdService = jsonLdService;
    }

    /**
     * Serializes a {@link RemoteMessage} to JSON-LD. The message is first transformed using the
     * {@link TypeTransformerRegistry}, then the resulting JSON-LD structure is compacted using
     * the given JSON-LD context before returning it as a string.
     *
     * @param message the message to serialize
     * @return the serialized message
     */
    @Override
    public String serialize(RemoteMessage message) {
        try {
            var transformResult = registry.transform(message, JsonObject.class);

            if (transformResult.succeeded()) {
                var compacted = jsonLdService.compact(transformResult.getContent());
                if (compacted.succeeded()) {
                    return mapper.writeValueAsString(compacted.getContent());
                }
                throw new EdcException("Failed to compact JSON-LD: " + compacted.getFailureDetail());
            }
            throw new EdcException(format("Failed to transform %s: %s", message.getClass().getSimpleName(), join(", ", transformResult.getFailureMessages())));
        } catch (JsonProcessingException e) {
            throw new EdcException(format("Failed to serialize %s", message.getClass().getSimpleName()), e);
        }
    }
}

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

package org.eclipse.edc.connector.api.management.policy.transform;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.policy.spi.PolicyDefinition;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.connector.policy.spi.PolicyDefinition.EDC_POLICY_DEFINITION_POLICY;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.EDC_CREATED_AT;

public class JsonObjectFromPolicyDefinitionTransformer extends AbstractJsonLdTransformer<PolicyDefinition, JsonObject> {

    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromPolicyDefinitionTransformer(JsonBuilderFactory jsonFactory) {
        super(PolicyDefinition.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull PolicyDefinition input, @NotNull TransformerContext context) {
        var objectBuilder = jsonFactory.createObjectBuilder();
        objectBuilder.add(ID, input.getId());
        objectBuilder.add(TYPE, PolicyDefinition.EDC_POLICY_DEFINITION_TYPE);

        objectBuilder.add(EDC_CREATED_AT, input.getCreatedAt());

        var policy = context.transform(input.getPolicy(), JsonObject.class);
        objectBuilder.add(EDC_POLICY_DEFINITION_POLICY, policy);

        return objectBuilder.build();
    }
}

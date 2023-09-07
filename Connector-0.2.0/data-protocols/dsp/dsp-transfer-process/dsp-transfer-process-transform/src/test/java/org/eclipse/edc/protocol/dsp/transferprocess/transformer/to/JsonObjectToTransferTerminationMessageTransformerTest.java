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

package org.eclipse.edc.protocol.dsp.transferprocess.transformer.to;

import jakarta.json.Json;
import org.eclipse.edc.protocol.dsp.transferprocess.transformer.type.to.JsonObjectToTransferTerminationMessageTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_SCHEMA;
import static org.eclipse.edc.protocol.dsp.transferprocess.transformer.to.TestInput.getExpanded;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CODE;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_REASON;
import static org.eclipse.edc.protocol.dsp.type.DspTransferProcessPropertyAndTypeNames.DSPACE_TYPE_TRANSFER_TERMINATION_MESSAGE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JsonObjectToTransferTerminationMessageTransformerTest {

    private final String processId = "TestProcessId";

    private final String code = "testCode";

    private TransformerContext context = mock(TransformerContext.class);

    private JsonObjectToTransferTerminationMessageTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectToTransferTerminationMessageTransformer();
    }

    @Test
    void jsonObjectToTransferTerminationMessage() {
        var reason = Json.createBuilderFactory(Map.of()).createObjectBuilder().add(DSPACE_SCHEMA + "foo", "bar");
        var reasonArray = Json.createBuilderFactory(Map.of()).createArrayBuilder().add(reason).build();

        var json = Json.createObjectBuilder()
                .add(TYPE, DSPACE_TYPE_TRANSFER_TERMINATION_MESSAGE)
                .add(DSPACE_PROPERTY_PROCESS_ID, "TestProcessId")
                .add(DSPACE_PROPERTY_CODE, "testCode")
                .add(DSPACE_PROPERTY_REASON, Json.createBuilderFactory(Map.of()).createArrayBuilder().add(reasonArray).build())
                .build();

        var result = transformer.transform(getExpanded(json), context);

        assertThat(result).isNotNull();

        assertThat(result.getProcessId()).isEqualTo("TestProcessId");
        assertThat(result.getReason()).isEqualTo(format("[{\"%sfoo\":[{\"@value\":\"bar\"}]}]", DSPACE_SCHEMA));
        assertThat(result.getCode()).isEqualTo("testCode");

        verify(context, never()).reportProblem(anyString());
    }
}

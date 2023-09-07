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

package org.eclipse.edc.protocol.dsp.negotiation.transform.from;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.ACCEPTED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.ACCEPTING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.AGREED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.AGREEING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.FINALIZED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.FINALIZING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.OFFERED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.OFFERING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.REQUESTED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.REQUESTING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.TERMINATED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.TERMINATING;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.VERIFIED;
import static org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates.VERIFYING;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_NEGOTIATION;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_ACCEPTED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_AGREED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_FINALIZED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_OFFERED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_REQUESTED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_TERMINATED;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_VALUE_NEGOTIATION_STATE_VERIFIED;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_STATE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JsonObjectFromContractNegotiationTransformerTest {

    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final TransformerContext context = mock(TransformerContext.class);

    private JsonObjectFromContractNegotiationTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectFromContractNegotiationTransformer(jsonFactory);
    }

    @Test
    void transform() {
        var value = "example";
        var negotiation = ContractNegotiation.Builder.newInstance()
                .id(value)
                .correlationId(value)
                .counterPartyId("counterPartyId")
                .counterPartyAddress("counterPartyAddress")
                .protocol("protocol")
                .state(REQUESTED.code())
                .build();

        var result = transformer.transform(negotiation, context);

        assertThat(result).isNotNull();
        assertThat(result.getString(ID)).isEqualTo(value);
        assertThat(result.getString(TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION);
        assertThat(result.getString(DSPACE_PROPERTY_PROCESS_ID)).isEqualTo(value);

        verify(context, never()).reportProblem(anyString());
    }

    @ParameterizedTest
    @ArgumentsSource(Status.class)
    void transform_status(ContractNegotiationStates inputState, String expectedDspState) {
        var value = "example";
        var negotiation = ContractNegotiation.Builder.newInstance()
                .id(value)
                .correlationId(value)
                .counterPartyId("counterPartyId")
                .counterPartyAddress("counterPartyAddress")
                .protocol("protocol")
                .state(inputState.code())
                .build();

        var result = transformer.transform(negotiation, context);

        assertThat(result).isNotNull();
        assertThat(result.getString(DSPACE_PROPERTY_STATE)).isEqualTo(expectedDspState);

        verify(context, never()).reportProblem(anyString());
    }

    public static class Status implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.arguments(REQUESTING, DSPACE_VALUE_NEGOTIATION_STATE_REQUESTED),
                    Arguments.arguments(REQUESTED, DSPACE_VALUE_NEGOTIATION_STATE_REQUESTED),
                    Arguments.arguments(OFFERING, DSPACE_VALUE_NEGOTIATION_STATE_OFFERED),
                    Arguments.arguments(OFFERED, DSPACE_VALUE_NEGOTIATION_STATE_OFFERED),
                    Arguments.arguments(ACCEPTING, DSPACE_VALUE_NEGOTIATION_STATE_ACCEPTED),
                    Arguments.arguments(ACCEPTED, DSPACE_VALUE_NEGOTIATION_STATE_ACCEPTED),
                    Arguments.arguments(AGREEING, DSPACE_VALUE_NEGOTIATION_STATE_AGREED),
                    Arguments.arguments(AGREED, DSPACE_VALUE_NEGOTIATION_STATE_AGREED),
                    Arguments.arguments(VERIFYING, DSPACE_VALUE_NEGOTIATION_STATE_VERIFIED),
                    Arguments.arguments(VERIFIED, DSPACE_VALUE_NEGOTIATION_STATE_VERIFIED),
                    Arguments.arguments(FINALIZING, DSPACE_VALUE_NEGOTIATION_STATE_FINALIZED),
                    Arguments.arguments(FINALIZED, DSPACE_VALUE_NEGOTIATION_STATE_FINALIZED),
                    Arguments.arguments(TERMINATING, DSPACE_VALUE_NEGOTIATION_STATE_TERMINATED),
                    Arguments.arguments(TERMINATED, DSPACE_VALUE_NEGOTIATION_STATE_TERMINATED)
            );
        }
    }
}

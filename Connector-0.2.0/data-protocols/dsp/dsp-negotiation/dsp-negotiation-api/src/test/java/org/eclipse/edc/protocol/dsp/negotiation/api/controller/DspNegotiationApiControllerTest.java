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

package org.eclipse.edc.protocol.dsp.negotiation.api.controller;

import io.restassured.specification.RequestSpecification;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreementMessage;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreementVerificationMessage;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractNegotiationEventMessage;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationTerminationMessage;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractOfferMessage;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractRequestMessage;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.connector.contract.spi.types.protocol.ContractRemoteMessage;
import org.eclipse.edc.connector.spi.contractnegotiation.ContractNegotiationProtocolService;
import org.eclipse.edc.jsonld.spi.JsonLdKeywords;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.service.spi.result.ServiceResult;
import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.contract.spi.types.agreement.ContractNegotiationEventMessage.Type.ACCEPTED;
import static org.eclipse.edc.connector.contract.spi.types.agreement.ContractNegotiationEventMessage.Type.FINALIZED;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.AGREEMENT;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.BASE_PATH;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.CONTRACT_OFFER;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.CONTRACT_REQUEST;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.EVENT;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.INITIAL_CONTRACT_REQUEST;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.TERMINATION;
import static org.eclipse.edc.protocol.dsp.negotiation.api.NegotiationApiPaths.VERIFICATION;
import static org.eclipse.edc.protocol.dsp.spi.types.HttpMessageProtocol.DATASPACE_PROTOCOL_HTTP;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_AGREEMENT_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_AGREEMENT_VERIFICATION_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_NEGOTIATION_EVENT_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_NEGOTIATION_TERMINATION_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_OFFER_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CODE;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_REASON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ApiTest
class DspNegotiationApiControllerTest extends RestControllerTestBase {

    private final IdentityService identityService = mock(IdentityService.class);
    private final TypeTransformerRegistry registry = mock(TypeTransformerRegistry.class);
    private final ContractNegotiationProtocolService protocolService = mock(ContractNegotiationProtocolService.class);

    private final String callbackAddress = "http://callback";
    private final JsonObject request = Json.createObjectBuilder()
            .add("http://schema/key", "value")
            .build();

    private static ContractAgreementMessage contractAgreementMessage() {
        return ContractAgreementMessage.Builder.newInstance()
                .protocol("protocol")
                .processId("testId")
                .counterPartyAddress("http://connector")
                .contractAgreement(contractAgreement())
                .build();
    }

    private static ContractAgreement contractAgreement() {
        return ContractAgreement.Builder.newInstance()
                .id(randomUUID().toString())
                .providerId("agentId")
                .consumerId("agentId")
                .assetId("assetId")
                .policy(policy()).build();
    }

    private static ContractRequestMessage contractRequestMessage() {
        return ContractRequestMessage.Builder.newInstance()
                .protocol("protocol")
                .processId("testId")
                .counterPartyAddress("http://connector")
                .callbackAddress("http://connector")
                .dataSet("dataSet")
                .contractOffer(contractOffer())
                .build();
    }
    
    private static ContractOfferMessage contractOfferMessage() {
        return ContractOfferMessage.Builder.newInstance()
                .protocol("protocol")
                .processId("testId")
                .counterPartyAddress("http://connector")
                .callbackAddress("http://connector")
                .contractOffer(contractOffer())
                .build();
    }

    private static ContractOffer contractOffer() {
        return ContractOffer.Builder.newInstance()
                .id(String.valueOf(randomUUID()))
                .assetId("assetId")
                .policy(policy()).build();
    }

    private static Policy policy() {
        var action = Action.Builder.newInstance().type("USE").build();
        var permission = Permission.Builder.newInstance().action(action).build();
        var prohibition = Prohibition.Builder.newInstance().action(action).build();
        var duty = Duty.Builder.newInstance().action(action).build();
        return Policy.Builder.newInstance()
                .permission(permission)
                .prohibition(prohibition)
                .duty(duty)
                .build();
    }

    private static ContractAgreementVerificationMessage contractAgreementVerificationMessage() {
        return ContractAgreementVerificationMessage.Builder.newInstance()
                .protocol("protocol")
                .counterPartyAddress("http://connector")
                .processId("testId")
                .build();
    }

    private static ContractNegotiationEventMessage contractNegotiationEventMessage_accepted() {
        return ContractNegotiationEventMessage.Builder.newInstance()
                .protocol("protocol")
                .counterPartyAddress("http://connector")
                .processId("testId")
                .type(ACCEPTED)
                .build();
    }

    private static ContractNegotiationEventMessage contractNegotiationEventMessage_finalized() {
        return ContractNegotiationEventMessage.Builder.newInstance()
                .protocol("protocol")
                .counterPartyAddress("http://connector")
                .processId("testId")
                .type(FINALIZED)
                .build();
    }

    private static ContractNegotiationTerminationMessage contractNegotiationTerminationMessage() {
        return ContractNegotiationTerminationMessage.Builder.newInstance()
                .protocol("protocol")
                .counterPartyAddress("http://connector")
                .processId("testId")
                .rejectionReason("reason")
                .build();
    }

    @Test
    void getNegotiation_shouldReturnNegotiation_whenValidRequest() {
        var token = token();
        var id = "negotiationId";
        var negotiation = contractNegotiation();
        var json = Json.createObjectBuilder().build();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(protocolService.findById(id, token)).thenReturn(ServiceResult.success(negotiation));
        when(registry.transform(any(ContractNegotiation.class), eq(JsonObject.class))).thenReturn(Result.success(json));
        
        var result = baseRequest()
                .get(BASE_PATH + id)
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(200)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        verify(protocolService, times(1)).findById(id, token);
    }
    
    @Test
    void getNegotiation_shouldReturnNotFound_whenServiceResultNotFound() {
        var token = token();
        var id = "negotiationId";
        
        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(protocolService.findById(id, token)).thenReturn(ServiceResult.notFound("not found"));
        
        var result = baseRequest()
                .get(BASE_PATH + id)
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(404)
                .extract().as(JsonObject.class);
        
        assertThat(result).isNotNull();
        assertThat(result.getString(JsonLdKeywords.TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR);
        assertThat(result.getString(DSPACE_PROPERTY_CODE)).isEqualTo("404");
        assertThat(result.get(DSPACE_PROPERTY_PROCESS_ID)).isNotNull();
        assertThat(result.get(DSPACE_PROPERTY_REASON)).isNotNull();
        
        verify(protocolService, times(1)).findById(id, token);
    }

    @Test
    void initiateNegotiation_shouldReturnContractNegotiation_whenValidRequest() {
        var token = token();
        var message = contractRequestMessage();
        var process = contractNegotiation();
        var json = Json.createObjectBuilder().build();
        var request = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE).build();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), eq(ContractRequestMessage.class))).thenReturn(Result.success(message));
        when(protocolService.notifyRequested(message, token)).thenReturn(ServiceResult.success(process));
        when(registry.transform(any(ContractNegotiation.class), eq(JsonObject.class))).thenReturn(Result.success(json));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(BASE_PATH + INITIAL_CONTRACT_REQUEST)
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        verify(protocolService, times(1)).notifyRequested(message, token);
    }

    @Test
    void initiateNegotiation_shouldReturnInternalServerError_whenContractNegotiationTransformationFails() {
        var token = token();
        var message = contractRequestMessage();
        var process = contractNegotiation();
        var request = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE).build();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), eq(ContractRequestMessage.class))).thenReturn(Result.success(message));
        when(protocolService.notifyRequested(message, token)).thenReturn(ServiceResult.success(process));
        when(registry.transform(any(ContractNegotiation.class), eq(JsonObject.class))).thenReturn(Result.failure("error"));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(BASE_PATH + INITIAL_CONTRACT_REQUEST)
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(500)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        assertThat(result.getString(JsonLdKeywords.TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR);
        assertThat(result.getString(DSPACE_PROPERTY_CODE)).isEqualTo("500");
        assertThat(result.get(DSPACE_PROPERTY_PROCESS_ID)).isNotNull();
        assertThat(result.get(DSPACE_PROPERTY_REASON)).isNotNull();
    }

    /**
     * Verifies that an endpoint returns 401 if the identity service cannot verify the identity token.
     *
     * @param path the request path to the endpoint
     */
    @ParameterizedTest
    @ArgumentsSource(ControllerMethodArguments.class)
    void callEndpoint_shouldReturnUnauthorized_whenNotAuthorized(String path) {
        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.failure("error"));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(path)
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(401)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        assertThat(result.getString(JsonLdKeywords.TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR);
        assertThat(result.getString(DSPACE_PROPERTY_CODE)).isEqualTo("401");
        assertThat(result.get(DSPACE_PROPERTY_REASON)).isNotNull();

        if (!path.equals(BASE_PATH + INITIAL_CONTRACT_REQUEST)) {
            assertThat(result.get(DSPACE_PROPERTY_PROCESS_ID)).isNotNull();
        }
    }

    /**
     * Verifies that an endpoint returns 400 if the incoming message cannot be transformed.
     *
     * @param path the request path to the endpoint
     */
    @ParameterizedTest
    @ArgumentsSource(ControllerMethodArguments.class)
    void callEndpoint_shouldReturnBadRequest_whenRequestTransformationFails(String path) {
        var token = token();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), argThat(ContractRemoteMessage.class::isAssignableFrom))).thenReturn(Result.failure("error"));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(path)
                .then()
                .statusCode(400)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        assertThat(result.getString(JsonLdKeywords.TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR);
        assertThat(result.getString(DSPACE_PROPERTY_CODE)).isEqualTo("400");
        assertThat(result.get(DSPACE_PROPERTY_REASON)).isNotNull();

        if (!path.equals(BASE_PATH + INITIAL_CONTRACT_REQUEST)) {
            assertThat(result.get(DSPACE_PROPERTY_PROCESS_ID)).isNotNull();
        }
    }

    /**
     * Verifies that an endpoint returns 204 if the call to the service was successful. Also verifies
     * that the correct service method was called. This is only applicable for endpoints that do not
     * return a response body.
     *
     * @param path          the request path to the endpoint
     * @param message       the request message to be returned by the transformer registry
     * @param request       the request body for a defined endpoint
     * @param serviceMethod reference to the service method that should be called, required to verify that it was called
     */
    @ParameterizedTest
    @ArgumentsSource(ControllerMethodArgumentsForServiceCall.class)
    void callEndpoint_shouldCallService_whenValidRequest(String path, ContractRemoteMessage message, JsonObject request, Method serviceMethod) throws Exception {
        var token = token();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), argThat(ContractRemoteMessage.class::isAssignableFrom))).thenReturn(Result.success(message));

        when(protocolService.notifyRequested(any(ContractRequestMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyFinalized(any(ContractNegotiationEventMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyAccepted(any(ContractNegotiationEventMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyVerified(any(ContractAgreementVerificationMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyTerminated(any(ContractNegotiationTerminationMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyAgreed(any(ContractAgreementMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));
        when(protocolService.notifyOffered(any(ContractOfferMessage.class), eq(token))).thenReturn(ServiceResult.success(contractNegotiation()));

        baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(path)
                .then()
                .statusCode(200);

        var verify = verify(protocolService, times(1));
        serviceMethod.invoke(verify, message, token);

        // verify that the message protocol was set to the DSP protocol by the controller
        assertThat(message.getProtocol()).isEqualTo(DATASPACE_PROTOCOL_HTTP);

    }

    /**
     * Verifies that an endpoint returns 500 if there is an error in the service. Also verifies
     * that the correct service method was called.
     *
     * @param path          the request path to the endpoint
     * @param message       the request message to be returned by the transformer registry
     * @param request       the request body for a defined endpoint
     * @param serviceMethod reference to the service method that should be called, required to verify that it was called
     */
    @ParameterizedTest
    @ArgumentsSource(ControllerMethodArgumentsForServiceError.class)
    void callEndpoint_shouldReturnInternalServerError_whenServiceResultConflict(String path, ContractRemoteMessage message, JsonObject request, Method serviceMethod) throws Exception {
        var token = token();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), argThat(ContractRemoteMessage.class::isAssignableFrom))).thenReturn(Result.success(message));

        when(protocolService.notifyRequested(any(ContractRequestMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyFinalized(any(ContractNegotiationEventMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyAccepted(any(ContractNegotiationEventMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyVerified(any(ContractAgreementVerificationMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyTerminated(any(ContractNegotiationTerminationMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyAgreed(any(ContractAgreementMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));
        when(protocolService.notifyOffered(any(ContractOfferMessage.class), eq(token))).thenReturn(ServiceResult.conflict("error"));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(path)
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(409);

        var verify = verify(protocolService, times(1));
        serviceMethod.invoke(verify, message, token);

        assertThat(result).isNotNull();

        // verify that the message protocol was set to the DSP protocol by the controller
        assertThat(message.getProtocol()).isEqualTo(DATASPACE_PROTOCOL_HTTP);

    }

    /**
     * Verifies that an endpoint returns 500 if there is an error in the service. Also verifies
     * that the correct service method was called.
     *
     * @param path    the request path to the endpoint
     * @param message the request message to be returned by the transformer registry
     * @param request the request body for a defined endpoint
     */
    @ParameterizedTest
    @ArgumentsSource(ControllerMethodArgumentsForIdValidationFails.class)
    void callEndpoint_shouldReturnBadRequest_whenValidationFails(String path, ContractRemoteMessage message, JsonObject request) throws Exception {
        var token = token();

        when(identityService.verifyJwtToken(any(TokenRepresentation.class), eq(callbackAddress))).thenReturn(Result.success(token));
        when(registry.transform(any(JsonObject.class), argThat(ContractRemoteMessage.class::isAssignableFrom))).thenReturn(Result.success(message));

        var result = baseRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .post(path)
                .then()
                .statusCode(400)
                .extract().as(JsonObject.class);

        assertThat(result).isNotNull();
        assertThat(result.getString(JsonLdKeywords.TYPE)).isEqualTo(DSPACE_TYPE_CONTRACT_NEGOTIATION_ERROR);
        assertThat(result.getString(DSPACE_PROPERTY_CODE)).isEqualTo("400");
        assertThat(result.get(DSPACE_PROPERTY_PROCESS_ID)).isNotNull();
        assertThat(result.get(DSPACE_PROPERTY_REASON)).isNotNull();

        // verify that the message protocol was set to the DSP protocol by the controller
        assertThat(message.getProtocol()).isEqualTo(DATASPACE_PROTOCOL_HTTP);
    }

    @Override
    protected Object controller() {
        return new DspNegotiationApiController(callbackAddress, identityService, registry, protocolService, mock(Monitor.class));
    }

    private RequestSpecification baseRequest() {
        var authHeader = "auth";
        return given()
                .baseUri("http://localhost:" + port)
                .basePath("/")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .when();
    }

    private ContractNegotiation contractNegotiation() {
        return ContractNegotiation.Builder.newInstance()
                .id(randomUUID().toString())
                .type(ContractNegotiation.Type.PROVIDER)
                .correlationId("testId")
                .counterPartyId("connectorId")
                .counterPartyAddress("callbackAddress")
                .protocol("protocol")
                .state(400)
                .stateTimestamp(Instant.now().toEpochMilli())
                .build();
    }

    private static class ControllerMethodArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(BASE_PATH + INITIAL_CONTRACT_REQUEST),
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_REQUEST),
                    Arguments.of(BASE_PATH + "testId" + EVENT),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT + VERIFICATION),
                    Arguments.of(BASE_PATH + "testId" + TERMINATION),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT),
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_OFFER)
            );
        }
    }

    private static class ControllerMethodArgumentsForServiceCall implements ArgumentsProvider {
        JsonObject contractRequest = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE).build();
        JsonObject negotiationEvent = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_EVENT_MESSAGE).build();
        JsonObject agreementVerification = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_VERIFICATION_MESSAGE).build();
        JsonObject negotiationTermination = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_TERMINATION_MESSAGE).build();
        JsonObject contractAgreement = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_MESSAGE).build();
        JsonObject contractOffer = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_OFFER_MESSAGE).build();

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_REQUEST, contractRequestMessage(), contractRequest,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyRequested", ContractRequestMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + EVENT, contractNegotiationEventMessage_accepted(), negotiationEvent,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyAccepted", ContractNegotiationEventMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + EVENT, contractNegotiationEventMessage_finalized(), negotiationEvent,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyFinalized", ContractNegotiationEventMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT + VERIFICATION, contractAgreementVerificationMessage(), agreementVerification,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyVerified", ContractAgreementVerificationMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + TERMINATION, contractNegotiationTerminationMessage(), negotiationTermination,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyTerminated", ContractNegotiationTerminationMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT, contractAgreementMessage(), contractAgreement,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyAgreed", ContractAgreementMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_OFFER, contractOfferMessage(), contractOffer,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyOffered", ContractOfferMessage.class, ClaimToken.class))
            );
        }
    }

    private static class ControllerMethodArgumentsForServiceError implements ArgumentsProvider {
        JsonObject contractRequest = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE).build();
        JsonObject negotiationEvent = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_EVENT_MESSAGE).build();
        JsonObject agreementVerification = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_VERIFICATION_MESSAGE).build();
        JsonObject negotiationTermination = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_TERMINATION_MESSAGE).build();
        JsonObject contractAgreement = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_MESSAGE).build();
        JsonObject contractOffer = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_OFFER_MESSAGE).build();

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(BASE_PATH + INITIAL_CONTRACT_REQUEST, contractRequestMessage(), contractRequest,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyRequested", ContractRequestMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_REQUEST, contractRequestMessage(), contractRequest,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyRequested", ContractRequestMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + EVENT, contractNegotiationEventMessage_accepted(), negotiationEvent,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyAccepted", ContractNegotiationEventMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + EVENT, contractNegotiationEventMessage_finalized(), negotiationEvent,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyFinalized", ContractNegotiationEventMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT + VERIFICATION, contractAgreementVerificationMessage(), agreementVerification,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyVerified", ContractAgreementVerificationMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + TERMINATION, contractNegotiationTerminationMessage(), negotiationTermination,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyTerminated", ContractNegotiationTerminationMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + AGREEMENT, contractAgreementMessage(), contractAgreement,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyAgreed", ContractAgreementMessage.class, ClaimToken.class)),
                    Arguments.of(BASE_PATH + "testId" + CONTRACT_OFFER, contractOfferMessage(), contractOffer,
                            ContractNegotiationProtocolService.class.getDeclaredMethod("notifyOffered", ContractOfferMessage.class, ClaimToken.class))
            );
        }
    }

    private static class ControllerMethodArgumentsForIdValidationFails implements ArgumentsProvider {
        JsonObject contractRequest = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE).build();
        JsonObject negotiationEvent = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_EVENT_MESSAGE).build();
        JsonObject agreementVerification = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_VERIFICATION_MESSAGE).build();
        JsonObject negotiationTermination = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_NEGOTIATION_TERMINATION_MESSAGE).build();
        JsonObject contractAgreement = Json.createObjectBuilder().add("@type", DSPACE_TYPE_CONTRACT_AGREEMENT_MESSAGE).build();

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(BASE_PATH + "invalidId" + CONTRACT_REQUEST, contractRequestMessage(), contractRequest),
                    Arguments.of(BASE_PATH + "invalidId" + EVENT, contractNegotiationEventMessage_accepted(), negotiationEvent),
                    Arguments.of(BASE_PATH + "invalidId" + EVENT, contractNegotiationEventMessage_finalized(), negotiationEvent),
                    Arguments.of(BASE_PATH + "invalidId" + AGREEMENT + VERIFICATION, contractAgreementVerificationMessage(), agreementVerification),
                    Arguments.of(BASE_PATH + "invalidId" + TERMINATION, contractNegotiationTerminationMessage(), negotiationTermination),
                    Arguments.of(BASE_PATH + "invalidId" + AGREEMENT, contractAgreementMessage(), contractAgreement)
            );
        }
    }

    private ClaimToken token() {
        return ClaimToken.Builder.newInstance().build();
    }

}

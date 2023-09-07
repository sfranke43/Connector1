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

package org.eclipse.edc.test.e2e.managementapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.http.ContentType.JSON;
import static jakarta.json.Json.createObjectBuilder;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.COMPLETED;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.DEPROVISIONED;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.REQUESTED;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.CONTEXT;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VOCAB;
import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.CoreConstants.EDC_PREFIX;
import static org.eclipse.edc.spi.types.domain.callback.CallbackAddress.EVENTS;
import static org.eclipse.edc.spi.types.domain.callback.CallbackAddress.IS_TRANSACTIONAL;
import static org.eclipse.edc.spi.types.domain.callback.CallbackAddress.URI;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.is;

@EndToEndTest
public class TransferProcessApiEndToEndTest extends BaseManagementApiEndToEndTest {

    @Test
    void getAll() {
        getStore().updateOrCreate(createTransferProcess("tp1"));
        getStore().updateOrCreate(createTransferProcess("tp2"));

        baseRequest()
                .contentType(JSON)
                .post("/v2/transferprocesses/request")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].@id", anyOf(is("tp1"), is("tp2")))
                .body("[1].@id", anyOf(is("tp1"), is("tp2")));
    }

    @Test
    void getById() {
        getStore().updateOrCreate(createTransferProcess("tp1"));
        getStore().updateOrCreate(createTransferProcess("tp2"));

        baseRequest()
                .get("/v2/transferprocesses/tp2")
                .then()
                .statusCode(200)
                .body("@id", is("tp2"))
                .body(TYPE, is("edc:TransferProcess"));
    }

    @Test
    void getState() {
        getStore().updateOrCreate(createTransferProcessBuilder("tp2").state(COMPLETED.code()).build());

        baseRequest()
                .get("/v2/transferprocesses/tp2/state")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(TYPE, is("edc:TransferState"))
                .body("'edc:state'", is("COMPLETED"));
    }

    @Test
    void create() {
        var requestBody = createObjectBuilder()
                .add(CONTEXT, createObjectBuilder().add(EDC_PREFIX, EDC_NAMESPACE))
                .add(TYPE, "TransferRequest")
                .add("dataDestination", createObjectBuilder()
                        .add(TYPE, "DataAddress")
                        .add("type", "HttpData")
                        .add("properties", createObjectBuilder()
                                .add("baseUrl", "http://any")
                                .build())
                        .build()
                )
                .add("callbackAddresses", createCallbackAddress())
                .add("protocol", "dataspace-protocol-http")
                .add("connectorAddress", "http://connector-address")
                .add("connectorId", "connectorId")
                .add("contractId", "contractId")
                .add("assetId", "assetId")
                .build();

        var id = baseRequest()
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/transferprocesses/")
                .then()
                .log().ifError()
                .statusCode(200)
                .extract().jsonPath().getString(ID);

        assertThat(getStore().findById(id)).isNotNull();
    }

    @Test
    void deprovision() {
        var id = UUID.randomUUID().toString();
        getStore().updateOrCreate(createTransferProcessBuilder(id).state(COMPLETED.code()).build());

        baseRequest()
                .contentType(JSON)
                .post("/v2/transferprocesses/" + id + "/deprovision")
                .then()
                .statusCode(204);
    }

    @Test
    void terminate() {
        var id = UUID.randomUUID().toString();
        getStore().updateOrCreate(createTransferProcessBuilder(id).state(REQUESTED.code()).build());
        var requestBody = createObjectBuilder()
                .add(CONTEXT, createObjectBuilder().add(VOCAB, EDC_NAMESPACE))
                .add("reason", "any")
                .build();

        baseRequest()
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/transferprocesses/" + id + "/terminate")
                .then()
                .log().ifError()
                .statusCode(204);
    }

    @Test
    void query_byState() throws JsonProcessingException {

        var state = DEPROVISIONED;
        var tp = createTransferProcessBuilder("test-tp")
                .state(state.code())
                .build();
        getStore().save(tp);


        var content = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/"
                    },
                    "@type": "QuerySpec",
                    "filterExpression": [
                        {
                            "operandLeft": "state",
                            "operandRight": %d,
                            "operator": "="
                        }
                    ],
                    "limit": 100,
                    "offset": 0
                }
                """;
        content = format(content, state.code());
        JsonObject query = JacksonJsonLd.createObjectMapper()
                .readValue(content, JsonObject.class);

        var result = baseRequest()
                .contentType(JSON)
                .body(query)
                .post("/v2/transferprocesses/request")
                .then()
                .statusCode(200)
                .extract().body().as(JsonArray.class);

        assertThat(result).isNotEmpty();
        assertThat(result).anySatisfy(it -> assertThat(it.asJsonObject().getString("edc:state")).isEqualTo(state.toString()));
    }

    private TransferProcessStore getStore() {
        return controlPlane.getContext().getService(TransferProcessStore.class);
    }

    private TransferProcess createTransferProcess(String id) {
        return createTransferProcessBuilder(id).build();
    }

    private TransferProcess.Builder createTransferProcessBuilder(String id) {
        return TransferProcess.Builder.newInstance()
                .id(id)
                .callbackAddresses(List.of(CallbackAddress.Builder.newInstance().uri("http://any").events(emptySet()).build()))
                .dataRequest(DataRequest.Builder.newInstance()
                        .id(UUID.randomUUID().toString())
                        .dataDestination(DataAddress.Builder.newInstance()
                                .type("type")
                                .build())
                        .protocol("dataspace-protocol-http")
                        .assetId("asset-id")
                        .connectorId("connector-id")
                        .contractId("contractId")
                        .connectorAddress("http://connector/address")
                        .processId(id)
                        .build());
    }

    private JsonArrayBuilder createCallbackAddress() {
        var builder = Json.createArrayBuilder();
        return builder.add(createObjectBuilder()
                .add(IS_TRANSACTIONAL, false)
                .add(URI, "http://test.local/")
                .add(EVENTS, Json.createArrayBuilder().build()));
    }

}

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

package org.eclipse.edc.connector.api.management.catalog;

import jakarta.json.Json;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.connector.spi.catalog.CatalogService;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.validator.spi.JsonObjectValidatorRegistry;
import org.eclipse.edc.validator.spi.ValidationResult;
import org.eclipse.edc.validator.spi.Violation;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.eclipse.edc.spi.response.ResponseStatus.FATAL_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ApiTest
class CatalogApiControllerTest extends RestControllerTestBase {

    private final CatalogService service = mock();
    private final TypeTransformerRegistry transformerRegistry = mock();
    private final JsonObjectValidatorRegistry validatorRegistry = mock();

    @Override
    protected Object controller() {
        return new CatalogApiController(service, transformerRegistry, validatorRegistry);
    }

    @Test
    void requestCatalog() {
        var request = CatalogRequest.Builder.newInstance().providerUrl("http://url").build();
        when(validatorRegistry.validate(any(), any())).thenReturn(ValidationResult.success());
        when(transformerRegistry.transform(any(), eq(CatalogRequest.class))).thenReturn(Result.success(request));
        when(service.request(any(), any(), any())).thenReturn(completedFuture(StatusResult.success("{}".getBytes())));
        var requestBody = Json.createObjectBuilder().add(CatalogRequest.CATALOG_REQUEST_PROTOCOL, "any").build();

        given()
                .port(port)
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/catalog/request")
                .then()
                .statusCode(200)
                .contentType(JSON);
        verify(transformerRegistry).transform(any(), eq(CatalogRequest.class));
    }

    @Test
    void catalogRequest_shouldReturnBadRequest_whenValidationFails() {
        when(validatorRegistry.validate(any(), any())).thenReturn(ValidationResult.failure(Violation.violation("error", "path")));
        var requestBody = Json.createObjectBuilder().add(CatalogRequest.CATALOG_REQUEST_PROTOCOL, "any").build();

        given()
                .port(port)
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/catalog/request")
                .then()
                .statusCode(400);
        verify(validatorRegistry).validate(eq(CatalogRequest.CATALOG_REQUEST_TYPE), any());
        verifyNoInteractions(transformerRegistry, service);
    }

    @Test
    void catalogRequest_shouldReturnBadRequest_whenTransformFails() {
        when(validatorRegistry.validate(any(), any())).thenReturn(ValidationResult.success());
        when(transformerRegistry.transform(any(), eq(CatalogRequest.class))).thenReturn(Result.failure("error"));
        var requestBody = Json.createObjectBuilder().add(CatalogRequest.CATALOG_REQUEST_PROTOCOL, "any").build();

        given()
                .port(port)
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/catalog/request")
                .then()
                .statusCode(400);
        verifyNoInteractions(service);
    }

    @Test
    void requestCatalog_shouldReturnBadGateway_whenServiceFails() {
        var request = CatalogRequest.Builder.newInstance().providerUrl("http://url").build();
        when(validatorRegistry.validate(any(), any())).thenReturn(ValidationResult.success());
        when(transformerRegistry.transform(any(), eq(CatalogRequest.class))).thenReturn(Result.success(request));
        when(service.request(any(), any(), any())).thenReturn(completedFuture(StatusResult.failure(FATAL_ERROR, "error")));

        var requestBody = Json.createObjectBuilder().add(CatalogRequest.CATALOG_REQUEST_PROTOCOL, "any").build();

        given()
                .port(port)
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/catalog/request")
                .then()
                .statusCode(502);
    }

    @Test
    void requestCatalog_shouldReturnBadGateway_whenServiceThrowsException() {
        var request = CatalogRequest.Builder.newInstance().providerUrl("http://url").build();
        when(validatorRegistry.validate(any(), any())).thenReturn(ValidationResult.success());
        when(transformerRegistry.transform(any(), eq(CatalogRequest.class))).thenReturn(Result.success(request));
        when(service.request(any(), any(), any())).thenReturn(failedFuture(new EdcException("error")));
        var requestBody = Json.createObjectBuilder().add(CatalogRequest.CATALOG_REQUEST_PROTOCOL, "any").build();

        given()
                .port(port)
                .contentType(JSON)
                .body(requestBody)
                .post("/v2/catalog/request")
                .then()
                .statusCode(502);
    }

}
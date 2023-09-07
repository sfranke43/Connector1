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

package org.eclipse.edc.connector.dataplane.http.params;

import io.netty.handler.codec.http.HttpMethod;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.eclipse.edc.connector.dataplane.http.testfixtures.HttpTestFixtures.formatRequestBodyAsString;

class HttpRequestFactoryTest {

    private static final String SCHEME = "http";
    private static final String HOST = "some.base.url";
    private static final String BASE_URL = String.format("%s://%s", SCHEME, HOST);

    private final HttpRequestFactory paramsToRequest = new HttpRequestFactory();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "\\", "/" })
    void verifyPathIgnoredWhenNullOrBlank(String p) {
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(HttpMethod.GET.name())
                .path(p)
                .build();

        var request = paramsToRequest.toRequest(params);

        assertBaseUrl(request.url().url());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void verifyQueryParamsIgnoredWhenNullOrBlank(String qp) {
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(HttpMethod.GET.name())
                .queryParams(qp)
                .build();

        var request = paramsToRequest.toRequest(params);

        assertBaseUrl(request.url().url());
    }

    @Test
    void verifyHeaders() {
        var headers = Map.of("key1", "value1");
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(HttpMethod.GET.name())
                .headers(headers)
                .build();

        var request = paramsToRequest.toRequest(params);

        assertThat(request.headers()).isNotNull();
        headers.forEach((s, s2) -> assertThat(request.header(s)).isNotNull().isEqualTo(s2));
    }

    @Test
    void verifyComplexUrl() {
        var path = "testpath";
        var queryParams = "test-queryparams";
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(HttpMethod.GET.name())
                .path(path)
                .queryParams(queryParams)
                .build();

        var httpRequest = paramsToRequest.toRequest(params);

        var url = httpRequest.url().url();
        assertBaseUrl(url);
        assertThat(url.getPath()).isEqualTo("/" + path);
        assertThat(url.getQuery()).isEqualTo(queryParams);
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.GET.name());
    }

    @Test
    void verifyDefaultContentTypeIsOctetStream() {
        var body = "Test body";
        var contentType = "application/octet-stream";
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .body(body)
                .build();

        var request = paramsToRequest.toRequest(params);

        var requestBody = request.body();
        assertThat(requestBody).isNotNull();
        assertThat(requestBody.contentType()).hasToString(contentType);
    }

    @Test
    void verifyBodyFromParams() throws IOException {
        var body = "Test body";
        var contentType = "text/plain";
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .contentType(contentType)
                .body(body)
                .build();

        var request = paramsToRequest.toRequest(params);

        var requestBody = request.body();
        assertThat(requestBody).isNotNull();
        assertThat(requestBody.contentType()).hasToString(contentType);
        assertThat(formatRequestBodyAsString(requestBody)).isEqualTo(body);
    }


    @Test
    void verifyBodyFromMethodCall() throws IOException {
        var body = "Test body";
        var contentType = "application/json";
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .contentType(contentType)
                .build();

        var request = paramsToRequest.toRequest(params, () -> new ByteArrayInputStream(body.getBytes()));

        var requestBody = request.body();
        assertThat(requestBody).isNotNull();
        assertThat(requestBody.contentType()).hasToString(contentType);
        assertThat(formatRequestBodyAsString(requestBody)).isEqualTo(body);
    }

    @Test
    void verifyRequestBodyIsNullIfNoContentProvided() {
        var contentType = "test/content-type";
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(HttpMethod.GET.name())
                .contentType(contentType)
                .build();

        var request = paramsToRequest.toRequest(params);

        var requestBody = request.body();
        assertThat(requestBody).isNull();
    }

    @Test
    void verifyExceptionThrownIfBaseUrlMissing() {
        var builder = HttpRequestParams.Builder.newInstance().method(HttpMethod.GET.name());

        assertThatNullPointerException().isThrownBy(builder::build);
    }

    @Test
    void verifyExceptionThrownIfMethodMissing() {
        var builder = HttpRequestParams.Builder.newInstance().baseUrl(BASE_URL);

        assertThatNullPointerException().isThrownBy(builder::build);
    }

    @Test
    void verifyExceptionIsRaisedIfContentTypeIsNull() {
        var builder = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .contentType(null)
                .body("Test Body");

        assertThatNullPointerException().isThrownBy(builder::build);
    }

    @Test
    void verifyChunkedRequest() throws IOException {
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .nonChunkedTransfer(false)
                .build();

        var request = paramsToRequest.toRequest(params, () -> new ByteArrayInputStream("a body".getBytes()));

        var body = request.body();
        assertThat(body).isNotNull();
        assertThat(body.contentLength()).isEqualTo(-1);
    }

    @Test
    void verifyNotChunkedRequest() throws IOException {
        var params = HttpRequestParams.Builder.newInstance()
                .baseUrl(BASE_URL)
                .method(POST.name())
                .nonChunkedTransfer(true)
                .build();

        var request = paramsToRequest.toRequest(params, () -> new ByteArrayInputStream("a body".getBytes()));

        var body = request.body();
        assertThat(body).isNotNull();
        assertThat(body.contentLength()).isEqualTo(6);
    }

    private void assertBaseUrl(URL url) {
        assertThat(url.getProtocol()).isEqualTo(SCHEME);
        assertThat(url.getHost()).isEqualTo(HOST);
    }
}

/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - Initial implementation
 *
 */

package org.eclipse.edc.iam.did.resolution;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.edc.iam.did.spi.document.DidConstants;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.document.VerificationMethod;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.spi.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DidPublicKeyResolverImplTest {

    private static final String DID_URL = "did:web:example.com";

    private DidDocument didDocument;

    private final DidResolverRegistry resolverRegistry = mock(DidResolverRegistry.class);
    private final DidPublicKeyResolverImpl resolver = new DidPublicKeyResolverImpl(resolverRegistry);

    @BeforeEach
    public void setUp() throws JOSEException, IOException {
        var eckey = (ECKey) ECKey.parseFromPEMEncodedObjects(readFile("public_secp256k1.pem"));

        var vm = VerificationMethod.Builder.create()
                .id("#my-key1")
                .type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019)
                .publicKeyJwk(eckey.toPublicJWK().toJSONObject())
                .build();

        didDocument = DidDocument.Builder.newInstance()
                .verificationMethod(List.of(vm))
                .service(Collections.singletonList(new Service("#my-service1", "MyService", "http://doesnotexi.st")))
                .build();
    }

    @Test
    void resolve() {
        when(resolverRegistry.resolve(DID_URL)).thenReturn(Result.success(didDocument));

        var result = resolver.resolvePublicKey(DID_URL);

        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent()).isNotNull();
        verify(resolverRegistry).resolve(DID_URL);
    }

    @Test
    void resolve_didNotFound() {
        when(resolverRegistry.resolve(DID_URL)).thenReturn(Result.failure("Not found"));

        var result = resolver.resolvePublicKey(DID_URL);

        assertThat(result.failed()).isTrue();
        verify(resolverRegistry).resolve(DID_URL);
    }

    @Test
    void resolve_didDoesNotContainPublicKey() {
        didDocument.getVerificationMethod().clear();
        when(resolverRegistry.resolve(DID_URL)).thenReturn(Result.success(didDocument));

        var result = resolver.resolvePublicKey(DID_URL);

        assertThat(result.failed()).isTrue();
        verify(resolverRegistry).resolve(DID_URL);
    }

    @Test
    void resolve_didContainsMultipleKeys() throws JOSEException, IOException {
        var publicKey = (ECKey) ECKey.parseFromPEMEncodedObjects(readFile("public_secp256k1.pem"));
        var vm = VerificationMethod.Builder.create().id("second-key").type(DidConstants.JSON_WEB_KEY_2020).controller("")
                .publicKeyJwk(publicKey.toJSONObject())
                .build();
        didDocument.getVerificationMethod().add(vm);
        when(resolverRegistry.resolve(DID_URL)).thenReturn(Result.success(didDocument));

        var result = resolver.resolvePublicKey(DID_URL);

        assertThat(result.failed()).isTrue();
        verify(resolverRegistry).resolve(DID_URL);
    }

    @Test
    void resolve_publicKeyNotInPemFormat() {
        didDocument.getVerificationMethod().clear();
        var vm = VerificationMethod.Builder.create().id("second-key").type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019).controller("")
                .publicKeyJwk(Map.of("kty", "EC"))
                .build();
        didDocument.getVerificationMethod().add(vm);

        when(resolverRegistry.resolve(DID_URL)).thenReturn(Result.success(didDocument));

        var result = resolver.resolvePublicKey(DID_URL);

        assertThat(result.failed()).isTrue();
        verify(resolverRegistry).resolve(DID_URL);
    }

    public static String readFile(String filename) throws IOException {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            var s = new Scanner(Objects.requireNonNull(is)).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}

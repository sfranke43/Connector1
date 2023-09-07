/*
 *  Copyright (c) 2021 - 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.iam.did;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import org.eclipse.edc.iam.did.parser.EcPrivateKeyParserFunction;
import org.eclipse.edc.iam.did.parser.PrivateKeyWrapperParserFunction;
import org.eclipse.edc.iam.did.parser.RsaPrivateKeyParserFunction;
import org.eclipse.edc.iam.did.resolution.DidPublicKeyResolverImpl;
import org.eclipse.edc.iam.did.resolution.DidResolverRegistryImpl;
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;


@Provides({ DidResolverRegistry.class, DidPublicKeyResolver.class })
@Extension(value = IdentityDidCoreExtension.NAME)
public class IdentityDidCoreExtension implements ServiceExtension {

    public static final String NAME = "Identity Did Core";
    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var didResolverRegistry = new DidResolverRegistryImpl();
        context.registerService(DidResolverRegistry.class, didResolverRegistry);

        var publicKeyResolver = new DidPublicKeyResolverImpl(didResolverRegistry);
        context.registerService(DidPublicKeyResolver.class, publicKeyResolver);

        registerParsers(privateKeyResolver);
    }

    private void registerParsers(PrivateKeyResolver resolver) {
        resolver.addParser(RSAKey.class, new RsaPrivateKeyParserFunction());
        resolver.addParser(ECKey.class, new EcPrivateKeyParserFunction());
        resolver.addParser(PrivateKeyWrapper.class, new PrivateKeyWrapperParserFunction());
    }
}

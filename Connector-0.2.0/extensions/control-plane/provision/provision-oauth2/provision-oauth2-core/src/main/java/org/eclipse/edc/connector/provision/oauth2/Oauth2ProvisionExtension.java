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
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.connector.provision.oauth2;

import org.eclipse.edc.connector.transfer.spi.provision.ProvisionManager;
import org.eclipse.edc.connector.transfer.spi.provision.ResourceManifestGenerator;
import org.eclipse.edc.iam.oauth2.spi.client.Oauth2Client;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.time.Clock;

/**
 * This extension has been deprecated in favor of "data-plane-http-oauth2"
 *
 * @deprecated please use 'data-plane-http-oauth2' instead
 */
@Deprecated(since = "milestone9")
@Extension(value = Oauth2ProvisionExtension.NAME)
public class Oauth2ProvisionExtension implements ServiceExtension {
    static final String NAME = "Oauth2 Provision (DEPRECATED: please use 'data-plane-http-oauth2' instead)";

    @Inject
    private ResourceManifestGenerator resourceManifestGenerator;

    @Inject
    private ProvisionManager provisionManager;

    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Inject
    private Clock clock;

    @Inject
    private Oauth2Client client;

    @Inject
    private Vault vault;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        typeManager.registerTypes(Oauth2ResourceDefinition.class, Oauth2ProvisionedResource.class);

        resourceManifestGenerator.registerGenerator(new Oauth2ProviderResourceDefinitionGenerator());
        resourceManifestGenerator.registerGenerator(new Oauth2ConsumerResourceDefinitionGenerator());

        var requestFactory = new Oauth2CredentialsRequestFactory(privateKeyResolver, clock, vault, context.getMonitor());
        provisionManager.register(new Oauth2Provisioner(client, requestFactory));
    }

}

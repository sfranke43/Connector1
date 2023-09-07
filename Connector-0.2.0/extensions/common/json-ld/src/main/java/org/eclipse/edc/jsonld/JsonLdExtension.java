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

package org.eclipse.edc.jsonld;

import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.jsonld.spi.transformer.JsonLdTransformer;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.CoreConstants;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCAT_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCAT_SCHEMA;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCT_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCT_SCHEMA;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_SCHEMA;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_PREFIX;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;
import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.CoreConstants.EDC_PREFIX;
import static org.eclipse.edc.spi.CoreConstants.JSON_LD;

/**
 * Adds support for working with JSON-LD. Provides an ObjectMapper that works with Jakarta JSON-P
 * types through the TypeManager context {@link CoreConstants#JSON_LD} and a registry
 * for {@link JsonLdTransformer}s. The module also offers
 * functions for working with JSON-LD structures.
 */
@BaseExtension
@Extension(value = JsonLdExtension.NAME)
public class JsonLdExtension implements ServiceExtension {

    public static final String NAME = "JSON-LD Extension";
    public static final String EDC_JSONLD_DOCUMENT_PREFIX = "edc.jsonld.document";
    public static final String CONFIG_VALUE_PATH = "path";
    public static final String CONFIG_VALUE_URL = "url";

    private static final boolean DEFAULT_HTTP_HTTPS_RESOLUTION = false;
    @Setting(value = "If set enable http json-ld document resolution", type = "boolean", defaultValue = DEFAULT_HTTP_HTTPS_RESOLUTION + "")
    private static final String HTTP_ENABLE_SETTING = "edc.jsonld.http.enabled";
    @Setting(value = "If set enable https json-ld document resolution", type = "boolean", defaultValue = DEFAULT_HTTP_HTTPS_RESOLUTION + "")
    private static final String HTTPS_ENABLE_SETTING = "edc.jsonld.https.enabled";
    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        typeManager.registerContext(JSON_LD, JacksonJsonLd.createObjectMapper());
    }

    @Provider
    public JsonLd createJsonLdService(ServiceExtensionContext context) {
        var config = context.getConfig();
        var configuration = JsonLdConfiguration.Builder.newInstance()
                .httpEnabled(config.getBoolean(HTTP_ENABLE_SETTING, DEFAULT_HTTP_HTTPS_RESOLUTION))
                .httpsEnabled(config.getBoolean(HTTPS_ENABLE_SETTING, DEFAULT_HTTP_HTTPS_RESOLUTION))
                .build();
        var monitor = context.getMonitor();
        var service = new TitaniumJsonLd(monitor, configuration);
        service.registerNamespace(EDC_PREFIX, EDC_NAMESPACE);
        service.registerNamespace(DCAT_PREFIX, DCAT_SCHEMA);
        service.registerNamespace(DCT_PREFIX, DCT_SCHEMA);
        service.registerNamespace(ODRL_PREFIX, ODRL_SCHEMA);
        service.registerNamespace(DSPACE_PREFIX, DSPACE_SCHEMA);

        getResourceUri("document" + File.separator + "odrl.jsonld")
                .onSuccess(uri -> service.registerCachedDocument("http://www.w3.org/ns/odrl.jsonld", uri))
                .onFailure(failure -> monitor.warning("Failed to register cached json-ld document: " + failure.getFailureDetail()));

        registerCachedDocumentsFromConfig(context, service);

        return service;
    }

    private void registerCachedDocumentsFromConfig(ServiceExtensionContext context, TitaniumJsonLd service) {
        context.getConfig()
                .getConfig(EDC_JSONLD_DOCUMENT_PREFIX)
                .partition()
                .forEach(config -> {
                    var tuple = config.getRelativeEntries();
                    if (tuple.containsKey(CONFIG_VALUE_PATH) && tuple.containsKey(CONFIG_VALUE_URL)) {
                        service.registerCachedDocument(tuple.get(CONFIG_VALUE_URL), new File(tuple.get(CONFIG_VALUE_PATH)).toURI());
                    } else {
                        context.getMonitor().warning(format("Expected a '%s' and a '%s' entry for '%s.%s', but found only '%s'", CONFIG_VALUE_PATH, CONFIG_VALUE_URL, EDC_JSONLD_DOCUMENT_PREFIX, config.currentNode(), String.join("", tuple.keySet())));
                    }
                });
    }

    @NotNull
    private Result<URI> getResourceUri(String name) {
        var uri = getClass().getClassLoader().getResource(name);
        if (uri == null) {
            return Result.failure(format("Cannot find resource %s", name));
        }

        try {
            return Result.success(uri.toURI());
        } catch (URISyntaxException e) {
            return Result.failure(format("Cannot read resource %s: %s", name, e.getMessage()));
        }
    }


}

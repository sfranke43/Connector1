/*
 *  Copyright (c) 2020 - 2022 Bayerische Motoren Werke Aktiengesellschaft
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft - initial API and implementation
 *
 */

package org.eclipse.edc.connector.api.management.catalog;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.JsonObject;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import org.eclipse.edc.api.model.ApiCoreSchema;

import static org.eclipse.edc.catalog.spi.CatalogRequest.CATALOG_REQUEST_TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;

@OpenAPIDefinition
@Tag(name = "Catalog")
public interface CatalogApi {

    @Operation(
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CatalogRequestSchema.class))),
            responses = { @ApiResponse(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CatalogSchema.class)
                    ),
                    description = "Gets contract offers (=catalog) of a single connector") }
    )
    void requestCatalog(JsonObject request, @Suspended AsyncResponse response);

    @Schema(name = "CatalogRequest", example = CatalogRequestSchema.CATALOG_REQUEST_EXAMPLE)
    record CatalogRequestSchema(
            @Schema(name = TYPE, example = CATALOG_REQUEST_TYPE)
            String providerUrl,
            String protocol,
            ApiCoreSchema.QuerySpecSchema querySpec) {

        public static final String CATALOG_REQUEST_EXAMPLE = """
                {
                    "@context": { "edc": "https://w3id.org/edc/v0.0.1/ns/" },
                    "@type": "CatalogRequest",
                    "providerUrl": "http://provider-address",
                    "protocol": "dataspace-protocol-http",
                    "querySpec": {
                        "offset": 0,
                        "limit": 50,
                        "sortOrder": "DESC",
                        "sortField": "fieldName",
                        "filterExpression": []
                    }
                }
                """;
    }

    @Schema(name = "Catalog", description = "DCAT catalog", example = CatalogSchema.CATALOG_EXAMPLE)
    record CatalogSchema(
    ) {
        public static final String CATALOG_EXAMPLE = """
                {
                    "@id": "7df65569-8c59-4013-b1c0-fa14f6641bf2",
                    "@type": "dcat:Catalog",
                    "dcat:dataset": {
                        "@id": "bcca61be-e82e-4da6-bfec-9716a56cef35",
                        "@type": "dcat:Dataset",
                        "odrl:hasPolicy": {
                            "@id": "OGU0ZTMzMGMtODQ2ZS00ZWMxLThmOGQtNWQxNWM0NmI2NmY4:YmNjYTYxYmUtZTgyZS00ZGE2LWJmZWMtOTcxNmE1NmNlZjM1:NDY2ZTZhMmEtNjQ1Yy00ZGQ0LWFlZDktMjdjNGJkZTU4MDNj",
                            "@type": "odrl:Set",
                            "odrl:permission": {
                                "odrl:target": "bcca61be-e82e-4da6-bfec-9716a56cef35",
                                "odrl:action": {
                                    "odrl:type": "http://www.w3.org/ns/odrl/2/use"
                                },
                                "odrl:constraint": {
                                    "odrl:and": [
                                        {
                                            "odrl:leftOperand": "https://w3id.org/edc/v0.0.1/ns/inForceDate",
                                            "odrl:operator": {
                                                "@id": "odrl:gteq"
                                            },
                                            "odrl:rightOperand": "2023-07-07T07:19:58.585601395Z"
                                        },
                                        {
                                            "odrl:leftOperand": "https://w3id.org/edc/v0.0.1/ns/inForceDate",
                                            "odrl:operator": {
                                                "@id": "odrl:lteq"
                                            },
                                            "odrl:rightOperand": "2023-07-12T07:19:58.585601395Z"
                                        }
                                    ]
                                }
                            },
                            "odrl:prohibition": [],
                            "odrl:obligation": [],
                            "odrl:target": "bcca61be-e82e-4da6-bfec-9716a56cef35"
                        },
                        "dcat:distribution": [
                            {
                                "@type": "dcat:Distribution",
                                "dct:format": {
                                    "@id": "HttpData"
                                },
                                "dcat:accessService": "5e839777-d93e-4785-8972-1005f51cf367"
                            }
                        ],
                        "edc:description": "description",
                        "edc:id": "bcca61be-e82e-4da6-bfec-9716a56cef35"
                    },
                    "dcat:service": {
                        "@id": "5e839777-d93e-4785-8972-1005f51cf367",
                        "@type": "dcat:DataService",
                        "dct:terms": "connector",
                        "dct:endpointUrl": "http://localhost:16806/protocol"
                    },
                    "edc:participantId": "urn:connector:provider",
                    "@context": {
                        "dct": "https://purl.org/dc/terms/",
                        "edc": "https://w3id.org/edc/v0.0.1/ns/",
                        "dcat": "https://www.w3.org/ns/dcat/",
                        "odrl": "http://www.w3.org/ns/odrl/2/",
                        "dspace": "https://w3id.org/dspace/v0.8/"
                    }
                }
                """;
    }
}

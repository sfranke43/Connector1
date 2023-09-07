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

package org.eclipse.edc.connector.dataplane.selector.api.v2;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.edc.connector.dataplane.selector.api.v2.schemas.DataPlaneInstanceSchema;
import org.eclipse.edc.connector.dataplane.selector.api.v2.schemas.SelectionRequestSchema;
import org.eclipse.edc.web.spi.ApiErrorDetail;

@OpenAPIDefinition
@Tag(name = "Dataplane Selector")
public interface DataplaneSelectorApi {
    @Operation(method = "POST",
            description = "Finds the best fitting data plane instance for a particular query",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = SelectionRequestSchema.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The DataPlane instance that fits best for the given selection request",
                            content = @Content(schema = @Schema(implementation = DataPlaneInstanceSchema.class))),
                    @ApiResponse(responseCode = "204", description = "No suitable DataPlane instance was found"),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class))))
            })
    @POST
    @Path("select")
    JsonObject find(JsonObject request);


    @Operation(method = "POST",
            description = "Adds one datatplane instance to the internal database of the selector",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = DataPlaneInstanceSchema.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Entry was added successfully to the database"),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class))))
            }
    )
    @POST
    void addEntry(JsonObject instance);

    @Operation(method = "GET",
            description = "Returns a list of all currently registered data plane instances",
            responses = {
                    @ApiResponse(responseCode = "204", description = "A (potentially empty) list of currently registered data plane instances"),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class))))
            }
    )
    @GET
    JsonArray getAll();


}

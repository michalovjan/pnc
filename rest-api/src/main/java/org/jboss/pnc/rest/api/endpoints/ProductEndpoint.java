/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.api.endpoints;

import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigurationPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductMilestonePage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductVersionPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductReleasePage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductMilestoneSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductVersionSingleton;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductReleaseSingleton;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.ws.rs.BeanParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product")
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProductEndpoint{
    static final String PD_ID = "ID of the product";

    @Operation(summary = "Gets all Products",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Creates a new Product",
            responses = {
                    @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductSingleton.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @POST
    Response createNew(Product product);

    @Operation(summary = "Gets specific Product",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductSingleton.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductSingleton.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    Response getSpecific(@Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Updates an existing Product",
            responses = {
                @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_CODE),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PUT
    @Path("/{id}")
    Response update(@Parameter(description = PD_ID, required = true) @PathParam("id") Integer id,
            @NotNull @Valid Product product);

    @Operation(summary = "Get all versions for a Product",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/versions")
    Response getProductVersions(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Creates a new version for a Product",
            responses = {
                    @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductVersionSingleton.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @POST
    @Path("/{id}/versions")
    Response createNewProductVersion(
            ProductVersion productVersion,
            @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Gets all milestones for a Product",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestonePage.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GET
    @Path("/{id}/milestones")
    Response getProductMilestones(@BeanParam PageParameters pageParameters,
            @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Creates a new milestone for a Product",
            responses = {
                    @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductMilestoneSingleton.class))),
                    @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @POST
    @Path("/{id}/milestones")
    Response createNewProductMilestone(
            ProductMilestone productMilestone,
            @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Gets all releases for a Product",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GET
    @Path("/{id}/releases")
    Response getAllProductReleases(@BeanParam PageParameters pageParameters,
        @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);

    @Operation(summary = "Creates a new release for a Product",
            responses = {
                    @ApiResponse(responseCode = ENTITY_CREATED_CODE, description = ENTITY_CREATED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ProductReleaseSingleton.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @POST
    @Path("/{id}/releases")
    Response createNewProductRelease(ProductRelease productRelease,
            @Parameter(description = PD_ID, required = true) @PathParam("id") Integer id);


    @Operation(summary = "Gets all Build Configs for a Product",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildConfigurationPage.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GET
    @Path("/{id}/build-configurations")
    Response getBuildConfigurations(@BeanParam PageParameters pageParams,
            @Parameter(description = PD_ID, required = true) @PathParam("id") int id);
}

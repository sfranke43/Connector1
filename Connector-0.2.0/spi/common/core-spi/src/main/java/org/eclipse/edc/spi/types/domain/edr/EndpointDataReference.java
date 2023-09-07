/*
 *  Copyright (c) 2020, 2021 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.spi.types.domain.edr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;

/**
 * Describes an endpoint serving data.
 */
@JsonDeserialize(builder = EndpointDataReference.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointDataReference {

    public static final String EDR_SIMPLE_TYPE = "EDR";
    public static final String EDR_TYPE = EDC_NAMESPACE + EDR_SIMPLE_TYPE;
    
    public static final String ID = EDC_NAMESPACE + "id";
    public static final String AUTH_CODE = EDC_NAMESPACE + "authCode";
    public static final String AUTH_KEY = EDC_NAMESPACE + "authKey";
    public static final String ENDPOINT = EDC_NAMESPACE + "endpoint";
    private final String id;
    private final String endpoint;
    private final String authKey;
    private final String authCode;
    private final Map<String, String> properties;

    private EndpointDataReference(String id, String endpoint, String authKey, String authCode, Map<String, String> properties) {
        this.id = id;
        this.endpoint = endpoint;
        this.authKey = authKey;
        this.authCode = authCode;
        this.properties = properties;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getEndpoint() {
        return endpoint;
    }

    @Nullable
    public String getAuthKey() {
        return authKey;
    }

    @Nullable
    public String getAuthCode() {
        return authCode;
    }

    @NotNull
    public Map<String, String> getProperties() {
        return properties;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final Map<String, String> properties = new HashMap<>();
        private String id = UUID.randomUUID().toString();
        private String endpoint;
        private String authKey;
        private String authCode;

        private Builder() {
        }

        @JsonCreator
        public static EndpointDataReference.Builder newInstance() {
            return new EndpointDataReference.Builder();
        }

        public EndpointDataReference.Builder id(String id) {
            this.id = id;
            return this;
        }

        public EndpointDataReference.Builder endpoint(String address) {
            this.endpoint = address;
            return this;
        }

        public EndpointDataReference.Builder authKey(String authKey) {
            this.authKey = authKey;
            return this;
        }

        public EndpointDataReference.Builder authCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public EndpointDataReference.Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public EndpointDataReference build() {
            Objects.requireNonNull(endpoint, "endpoint");
            if (authKey != null) {
                Objects.requireNonNull(authCode, "authCode");
            }
            if (authCode != null) {
                Objects.requireNonNull(authKey, "authKey");
            }
            return new EndpointDataReference(id, endpoint, authKey, authCode, properties);
        }
    }
}

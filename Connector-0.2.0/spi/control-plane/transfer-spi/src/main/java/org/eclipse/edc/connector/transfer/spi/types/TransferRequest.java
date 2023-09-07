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

package org.eclipse.edc.connector.transfer.spi.types;

import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;

public class TransferRequest {

    public static final String TRANSFER_REQUEST_TYPE = EDC_NAMESPACE + "TransferRequest";
    public static final String TRANSFER_REQUEST_CONNECTOR_ADDRESS = EDC_NAMESPACE + "connectorAddress";
    public static final String TRANSFER_REQUEST_CONTRACT_ID = EDC_NAMESPACE + "contractId";
    public static final String TRANSFER_REQUEST_DATA_DESTINATION = EDC_NAMESPACE + "dataDestination";
    public static final String TRANSFER_REQUEST_PROPERTIES = EDC_NAMESPACE + "properties";
    public static final String TRANSFER_REQUEST_PRIVATE_PROPERTIES = EDC_NAMESPACE + "privateProperties";
    public static final String TRANSFER_REQUEST_PROTOCOL = EDC_NAMESPACE + "protocol";
    public static final String TRANSFER_REQUEST_CONNECTOR_ID = EDC_NAMESPACE + "connectorId";
    public static final String TRANSFER_REQUEST_ASSET_ID = EDC_NAMESPACE + "assetId";
    public static final String TRANSFER_REQUEST_CALLBACK_ADDRESSES = EDC_NAMESPACE + "callbackAddresses";

    private String id;
    private String protocol;
    private String connectorAddress;
    private String connectorId;
    private String contractId;
    private String assetId;
    private DataAddress dataDestination;
    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> privateProperties = new HashMap<>();
    private List<CallbackAddress> callbackAddresses = new ArrayList<>();

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public String getId() {
        return id;
    }

    public String getContractId() {
        return contractId;
    }

    public DataAddress getDataDestination() {
        return dataDestination;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, String> getPrivateProperties() {
        return privateProperties;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public String getAssetId() {
        return assetId;
    }

    public List<CallbackAddress> getCallbackAddresses() {
        return callbackAddresses;
    }

    public static final class Builder {
        private final TransferRequest request;

        private Builder() {
            request = new TransferRequest();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder connectorAddress(String connectorAddress) {
            request.connectorAddress = connectorAddress;
            return this;
        }

        public Builder id(String id) {
            request.id = id;
            return this;
        }

        public Builder contractId(String contractId) {
            request.contractId = contractId;
            return this;
        }

        public Builder dataDestination(DataAddress dataDestination) {
            request.dataDestination = dataDestination;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            request.properties = properties;
            return this;
        }

        public Builder privateProperties(Map<String, String> privateProperties) {
            request.privateProperties = privateProperties;
            return this;
        }

        public Builder protocol(String protocol) {
            request.protocol = protocol;
            return this;
        }

        public Builder connectorId(String connectorId) {
            request.connectorId = connectorId;
            return this;
        }

        public Builder assetId(String assetId) {
            request.assetId = assetId;
            return this;
        }

        public Builder callbackAddresses(List<CallbackAddress> callbackAddresses) {
            request.callbackAddresses = callbackAddresses;
            return this;
        }

        public TransferRequest build() {
            return request;
        }
    }
}

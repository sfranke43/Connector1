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

package org.eclipse.edc.connector.transfer.spi.types.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.UUID.randomUUID;

/**
 * The {@link TransferRequestMessage} is sent by a consumer to initiate a transfer process.
 */
public class TransferRequestMessage implements TransferRemoteMessage {

    private String id;
    private String counterPartyAddress;
    private String protocol = "unknown";
    private String processId;
    private String contractId;
    private DataAddress dataDestination;
    private String callbackAddress;
    private Policy policy;

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        Objects.requireNonNull(protocol);
        this.protocol = protocol;
    }

    @Override
    public String getCounterPartyAddress() {
        return counterPartyAddress;
    }

    @Override
    @NotNull
    public String getProcessId() {
        return processId;
    }

    @Override
    public Policy getPolicy() {
        return policy;
    }

    public String getContractId() {
        return contractId;
    }

    public DataAddress getDataDestination() {
        return dataDestination;
    }

    public String getCallbackAddress() {
        return callbackAddress;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final TransferRequestMessage message;

        private Builder() {
            message = new TransferRequestMessage();
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(String id) {
            message.id = id;
            return this;
        }

        public Builder policy(Policy policy) {
            message.policy = policy;
            return this;
        }

        public Builder processId(String processId) {
            message.processId = processId;
            return this;
        }

        public Builder counterPartyAddress(String callbackAddress) {
            message.counterPartyAddress = callbackAddress;
            return this;
        }

        public Builder callbackAddress(String callbackAddress) {
            message.callbackAddress = callbackAddress;
            return this;
        }

        public Builder protocol(String protocol) {
            message.protocol = protocol;
            return this;
        }

        public Builder contractId(String contractId) {
            message.contractId = contractId;
            return this;
        }

        public Builder dataDestination(DataAddress dataDestination) {
            message.dataDestination = dataDestination;
            return this;
        }

        public TransferRequestMessage build() {
            if (message.id == null) {
                message.id = randomUUID().toString();
            }

            Objects.requireNonNull(message.callbackAddress, "The callbackAddress must be specified");
            return message;
        }
    }
}

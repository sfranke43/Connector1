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

package org.eclipse.edc.connector.transfer.spi.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * This event is raised when the TransferProcess has been cancelled.
 *
 * @deprecated this event is not thrown by anyone, please use {@link TransferProcessTerminated} instead
 */
@Deprecated(since = "milestone9")
@JsonDeserialize(builder = TransferProcessCancelled.Builder.class)
public class TransferProcessCancelled extends TransferProcessEvent {

    private TransferProcessCancelled() {
    }

    @Override
    public String name() {
        return "transfer.process.cancelled";
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends TransferProcessEvent.Builder<TransferProcessCancelled, Builder> {

        private Builder() {
            super(new TransferProcessCancelled());
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}

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

package org.eclipse.edc.connector.service.contractdefinition;

import org.eclipse.edc.connector.contract.spi.definition.observe.ContractDefinitionListener;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionCreated;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionDeleted;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionEvent;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionUpdated;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventRouter;

import java.time.Clock;

/**
 * Listener responsible for creating and publishing events regarding ContractDefinition state changes
 */
public class ContractDefinitionEventListener implements ContractDefinitionListener {
    private final Clock clock;
    private final EventRouter eventRouter;

    public ContractDefinitionEventListener(Clock clock, EventRouter eventRouter) {
        this.clock = clock;
        this.eventRouter = eventRouter;
    }

    @Override
    public void created(ContractDefinition contractDefinition) {
        var event = ContractDefinitionCreated.Builder.newInstance()
                .contractDefinitionId(contractDefinition.getId())
                .build();

        publish(event);
    }

    @Override
    public void deleted(ContractDefinition contractDefinition) {
        var event = ContractDefinitionDeleted.Builder.newInstance()
                .contractDefinitionId(contractDefinition.getId())
                .build();

        publish(event);
    }

    @Override
    public void updated(ContractDefinition contractDefinition) {
        var event = ContractDefinitionUpdated.Builder.newInstance()
                .contractDefinitionId(contractDefinition.getId())
                .build();

        publish(event);
    }

    private void publish(ContractDefinitionEvent event) {
        var envelope = EventEnvelope.Builder.newInstance()
                .payload(event)
                .at(clock.millis())
                .build();

        eventRouter.publish(envelope);
    }
}

/*
 *  Copyright (c) 2021 - 2022 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.connector.contract.negotiation.command.handlers;

import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.command.TerminateNegotiationCommand;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.spi.command.EntityCommandHandler;

/**
 * Handler for {@link TerminateNegotiationCommand}s. Transitions the specified ContractNegotiation to the TERMINATING state.
 */
public class TerminateNegotiationCommandHandler extends EntityCommandHandler<TerminateNegotiationCommand, ContractNegotiation> {

    public TerminateNegotiationCommandHandler(ContractNegotiationStore store) {
        super(store);
    }

    @Override
    public Class<TerminateNegotiationCommand> getType() {
        return TerminateNegotiationCommand.class;
    }

    /**
     * Transitions a {@link ContractNegotiation} to the error state.
     *
     * @param negotiation the ContractNegotiation to modify.
     * @return true
     */
    @Override
    protected boolean modify(ContractNegotiation negotiation, TerminateNegotiationCommand command) {
        negotiation.transitionTerminating(command.getReason());
        return true;
    }

}

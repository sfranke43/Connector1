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

package org.eclipse.edc.catalog.spi;

import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.query.QuerySpec;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Resolves {@link Dataset}s for the {@link Catalog}
 */
public interface DatasetResolver {

    /**
     * Resolves {@link Dataset}s given the {@link ParticipantAgent} and a {@link QuerySpec}
     *
     * @param agent the participant agent that requested the dataset.
     * @param querySpec the query spec for filtering and pagination.
     * @return a stream of datasets.
     */
    @NotNull
    Stream<Dataset> query(ParticipantAgent agent, QuerySpec querySpec);

    /**
     * Resolves a {@link Dataset} given its id
     *
     * @param participantAgent the participant agent that requested the dataset.
     * @param id the dataset id.
     * @return the {@link Dataset} if found, null otherwise.
     */
    Dataset getById(ParticipantAgent participantAgent, String id);
}

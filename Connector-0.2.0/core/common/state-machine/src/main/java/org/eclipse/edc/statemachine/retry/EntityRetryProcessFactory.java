/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.statemachine.retry;

import org.eclipse.edc.spi.entity.StatefulEntity;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;

import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Permits to instance {@link RetryProcess} implementations, that will permit a certain process acting on a {@link StatefulEntity}
 * to be tried again in case of failure. Please look at {@link RetryProcess} for further details.
 */
public class EntityRetryProcessFactory {
    private final Monitor monitor;
    private final EntityRetryProcessConfiguration configuration;
    private final Clock clock;

    public EntityRetryProcessFactory(Monitor monitor, Clock clock, EntityRetryProcessConfiguration configuration) {
        this.monitor = monitor;
        this.clock = clock;
        this.configuration = configuration;
    }

    /**
     * Initialize a simple process that needs to be retried if it does not succeed
     */
    public <T extends StatefulEntity<T>> SimpleRetryProcess<T> doSimpleProcess(T entity, Supplier<Boolean> process) {
        return new SimpleRetryProcess<>(entity, process, monitor, clock, configuration);
    }

    /**
     * Initialize a synchronous process that needs to be retried if it does not succeed
     */
    public <T extends StatefulEntity<T>, C> StatusResultRetryProcess<T, C> doSyncProcess(T entity, Supplier<StatusResult<C>> process) {
        return new StatusResultRetryProcess<>(entity, process, monitor, clock, configuration);
    }

    /**
     * Initialize an asynchronous process that needs to be retried if it does not succeed
     */
    public <T extends StatefulEntity<T>, C, SELF extends CompletableFutureRetryProcess<T, C, SELF>> SELF doAsyncProcess(T entity, Supplier<CompletableFuture<C>> process) {
        return (SELF) new CompletableFutureRetryProcess<T, C, SELF>(entity, process, monitor, clock, configuration);
    }

    /**
     * Initialize an asynchronous process that will return a {@link StatusResult} and it will need to be handled
     */
    public <T extends StatefulEntity<T>, C, SELF extends AsyncStatusResultRetryProcess<T, C, SELF>> SELF doAsyncStatusResultProcess(T entity, Supplier<CompletableFuture<StatusResult<C>>> process) {
        return (SELF) new AsyncStatusResultRetryProcess<T, C, SELF>(entity, process, monitor, clock, configuration);
    }

}

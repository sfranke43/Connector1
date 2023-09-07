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

import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.List;

/**
 * Resolves the {@link Distribution}s
 */
public interface DistributionResolver {

    /**
     * Return all the {@link Distribution}s for the given {@link Asset} and {@link DataAddress}
     *
     * @return a list of Distributions, always not null
     */
    List<Distribution> getDistributions(Asset asset, DataAddress dataAddress);
}

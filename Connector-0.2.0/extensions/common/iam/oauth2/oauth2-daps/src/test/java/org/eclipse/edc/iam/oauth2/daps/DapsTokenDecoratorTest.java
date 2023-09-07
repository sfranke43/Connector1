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

package org.eclipse.edc.iam.oauth2.daps;

import org.eclipse.edc.spi.iam.TokenParameters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DapsTokenDecoratorTest {

    private DapsTokenDecorator decorator;

    @Test
    void decorate() {
        decorator = new DapsTokenDecorator("test-scope");
        var bldr = TokenParameters.Builder.newInstance()
                .audience("test-audience");

        var result = decorator.decorate(bldr).build();

        assertThat(result.getAudience()).isEqualTo("test-audience");
        assertThat(result.getScope()).isEqualTo("test-scope");
    }

}
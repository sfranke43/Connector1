/*
 *  Copyright (c) 2021 Microsoft Corporation
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

package org.eclipse.edc.spi.types.domain;

import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;

class DataAddressTest {

    @Test
    void verifyDeserialization() throws IOException {
        var mapper = new TypeManager().getMapper();

        var dataAddress = DataAddress.Builder.newInstance()
                .type("test")
                .keyName("somekey")
                .property("foo", "bar").build();
        var writer = new StringWriter();
        mapper.writeValue(writer, dataAddress);

        var deserialized = mapper.readValue(writer.toString(), DataAddress.class);

        assertThat(deserialized).isNotNull();

        assertThat(deserialized.getType()).isEqualTo("test");
        assertThat(deserialized.getProperty("foo")).isEqualTo("bar");
    }

    @Test
    void verifyNoTypeThrowsException() {
        assertThatNullPointerException().isThrownBy(() -> DataAddress.Builder.newInstance()
                        .keyName("somekey")
                        .property("foo", "bar")
                        .build())
                .withMessageContaining("DataAddress builder missing Type property.");
    }

    @Test
    void verifyNullKeyThrowsException() {
        assertThatThrownBy(() -> DataAddress.Builder.newInstance().type("sometype").keyName("somekey").property(null, "bar").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property key null.");


        assertThatNullPointerException().isThrownBy(() -> DataAddress.Builder.newInstance()
                        .type("sometype")
                        .keyName("somekey")
                        .property(null, "bar")
                        .build())
                .withMessageContaining("Property key null.");
    }

    @Test
    void verifyGetDefaultPropertyValue() {
        assertThat(DataAddress.Builder.newInstance().type("sometype").build().getProperty("missing", "defaultValue"))
                .isEqualTo("defaultValue");
    }

    @Test
    void verifyGetExistingPropertyValue() {
        var address = DataAddress.Builder.newInstance()
                .type("sometype")
                .property("existing", "aValue")
                .property(EDC_NAMESPACE + "anotherExisting", "anotherValue")
                .build();

        assertThat(address.getProperty("existing", "defaultValue")).isEqualTo("aValue");
        assertThat(address.getProperty("anotherExisting", "defaultValue")).isEqualTo("anotherValue");
    }

    @Test
    void verifyHasProperty() {
        var address = DataAddress.Builder.newInstance()
                .type("sometype")
                .property("existing", "aValue")
                .property(EDC_NAMESPACE + "anotherExisting", "anotherValue")
                .build();

        assertThat(address.hasProperty("existing")).isTrue();
        assertThat(address.hasProperty("anotherExisting")).isTrue();
        assertThat(address.hasProperty("unknown")).isFalse();
    }
}

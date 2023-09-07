/*
 *  Copyright (c) 2020-2022 Microsoft Corporation
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
plugins {
    `java-library`
}

dependencies {
    api(project(":spi:data-plane-selector:data-plane-selector-spi"))
    api(project(":spi:common:http-spi"))
    implementation(project(":core:common:util"))
    implementation(project(":spi:common:transform-spi"))
    implementation(project(":extensions:common:json-ld"))

    testImplementation(project(":extensions:common:http"))
    testImplementation(project(":extensions:data-plane-selector:data-plane-selector-api"))
    testImplementation(project(":extensions:common:api:api-core"))

    testImplementation(project(":core:common:junit"))
    testImplementation(project(":core:common:transform-core"))
    testImplementation(project(":core:common:jersey-providers"))
    testImplementation(testFixtures(project(":extensions:common:http:jersey-core")))
}



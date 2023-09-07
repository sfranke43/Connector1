/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
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
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(project(":spi:control-plane:contract-spi"))
    api(project(":spi:control-plane:control-plane-spi"))
    api(project(":spi:control-plane:policy-spi"))
    api(project(":spi:common:transaction-spi"))
    api(project(":spi:common:policy-model"))
    implementation(project(":core:common:util"))
    implementation(project(":extensions:common:api:api-core"))
    implementation(project(":extensions:common:api:management-api-configuration"))
    implementation(project(":core:common:validator-core"))

    implementation(libs.jakarta.rsApi)

    testImplementation(project(":core:common:transform-core"))
    testImplementation(project(":core:control-plane:control-plane-core"))
    testImplementation(project(":core:data-plane-selector:data-plane-selector-core"))
    testImplementation(project(":extensions:common:http"))
    testImplementation(project(":extensions:common:transaction:transaction-local"))
    testImplementation(testFixtures(project(":extensions:common:http:jersey-core")))
    testImplementation(project(":core:common:junit"))
    testImplementation(libs.awaitility)
    testImplementation(libs.restAssured)
}

edcBuild {
    swagger {
        apiGroup.set("management-api")
    }
}



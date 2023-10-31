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

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
    }
}

dependencies {
	implementation(project(":core:control-plane:control-plane-core"))
	implementation(project(":data-protocols:dsp"))
	implementation(project(":extensions:common:configuration:configuration-filesystem"))
	implementation(project(":extensions:common:vault:vault-filesystem"))
	implementation(project(":extensions:common:iam:iam-mock"))
	implementation(project(":extensions:common:api:management-api-configuration"))
	implementation(project(":core:control-plane:transfer-core"))
	
	implementation(project(":extensions:data-plane-selector:data-plane-selector-api"))
	implementation(project(":extensions:data-plane-selector:data-plane-selector-client"))
	implementation(project(":core:data-plane-selector:data-plane-selector-core"))
	
	implementation(project(":extensions:data-plane:data-plane-api"))
	implementation(project(":core:data-plane:data-plane-core"))
	
                implementation(libs.edc.management.api)
	implementation(libs.edc.jsonld)
	
	
	//neue ZEile!
	implementation(project(":spi:control-plane:transfer-spi"))
	//implementation(project(":spi:common"))



    //implementation(libs.edc.control.plane.core)
    //implementation(libs.edc.dsp)
    //implementation(libs.edc.configuration.filesystem)
    //implementation(libs.edc.vault.filesystem)
    //implementation(libs.edc.iam.mock)
    //implementation(libs.edc.management.api)
    //implementation(libs.edc.transfer.data.plane)

    //implementation(libs.edc.data.plane.selector.api)
    //implementation(libs.edc.data.plane.selector.core)
    //implementation(libs.edc.data.plane.selector.client)

    //implementation(libs.edc.data.plane.api)
    //implementation(libs.edc.data.plane.core)
    implementation(project(":extensions:data-plane:data-plane-http"))
    //implementation('org.mock-server:mockserver-netty')
    //implementation group: 'org.mock-server', name: 'mockserver-client-java', version: '5.15.0'
}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("push-connector.jar")
    dependsOn(distTar, distZip)
}

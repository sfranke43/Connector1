plugins {
    `java-library`
}



dependencies {
    api(project(":spi:common:core-spi"))
    api(project(":spi:common:http-spi"))
    api(project(":spi:control-plane:control-plane-spi"))


    testImplementation(libs.mockserver.netty)
    testImplementation(libs.mockserver.client)
    testImplementation(project(":core:common:junit"))

}

package com.marineseo.fishtank

import org.slf4j.LoggerFactory
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import javax.servlet.ServletContext

class ServletInitializer : SpringBootServletInitializer() {
    private val initLogger = LoggerFactory.getLogger(this.javaClass.name)

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(FishWebServerApplication::class.java)
    }

    override fun onStartup(servletContext: ServletContext) {
        initLogger.info("onStartup")
        super.onStartup(servletContext)

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG")
    }
}

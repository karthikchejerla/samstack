package com.sksamuel.template.app

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.ktor.Cohort
import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import com.sksamuel.template.endpoints.module
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.IgnoreTrailingSlash
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

/**
 * In JVM apps, the main method is the entry point into the program.
 */
fun main() {
   logger.info(
      """
███╗   ███╗██╗   ██╗     ███████╗███████╗██████╗ ██╗   ██╗██╗ ██████╗███████╗
████╗ ████║╚██╗ ██╔╝     ██╔════╝██╔════╝██╔══██╗██║   ██║██║██╔════╝██╔════╝
██╔████╔██║ ╚████╔╝█████╗███████╗█████╗  ██████╔╝██║   ██║██║██║     █████╗
██║╚██╔╝██║  ╚██╔╝ ╚════╝╚════██║██╔══╝  ██╔══██╗╚██╗ ██╔╝██║██║     ██╔══╝
██║ ╚═╝ ██║   ██║        ███████║███████╗██║  ██║ ╚████╔╝ ██║╚██████╗███████╗
╚═╝     ╚═╝   ╚═╝        ╚══════╝╚══════╝╚═╝  ╚═╝  ╚═══╝  ╚═╝ ╚═════╝╚══════╝
"""
   )

//   val shutdownHook = EngineShutdownHook(10.seconds, 10.seconds, 30.seconds)
   val server = embeddedServer(Netty, port = Environment.config.port) {
      install(Compression)
      install(ContentNegotiation) { jackson() }
      install(IgnoreTrailingSlash)
      install(MicrometerMetrics) { this.registry = App.registry }
      install(Cohort) {
         this.gc = true
         this.jvmInfo = true
         this.operatingSystem = true
         this.threadDump = true
         this.heapDump = true
         healthcheck("/health", HealthCheckRegistry(Dispatchers.Default) {
            register(ThreadDeadlockHealthCheck(2), 15.seconds)
         })
      }
      // create your http modules here, passing in dependencies from the context object.
      // each module can be a set of related endpoints and plugins that you can easily test.
      // you may only have a single module for your entire app.
      module()
   }
//   shutdownHook.setEngine(server)
   server.start(true)
}

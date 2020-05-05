/*
 * MIT License
 *
 * Copyright (c) 2020 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.gooddata.example.db

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import java.net.ServerSocket
import javax.net.ServerSocketFactory


/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class MongoExtension: TestExecutionListener {

    private lateinit var mongoExecutable: MongodExecutable

    private var mongoPort: Int? = null

    override fun beforeTestClass(context: TestContext) {
        val ss: ServerSocket = ServerSocketFactory.getDefault().createServerSocket(0)
        val port = ss.localPort
        val config = MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(Net("localhost", port, Network.localhostIsIPv6()))
                .build()

        mongoExecutable = MongodStarter.getDefaultInstance().prepare(config)
        mongoPort = port

        if (context.applicationContext.environment is ConfigurableEnvironment) {
            val env = context.applicationContext.environment as ConfigurableEnvironment
            TestPropertyValues.of("spring.data.mongodb.port=$mongoPort").applyTo(env)
            TestPropertyValues.of("spring.data.mongodb.host=localhost").applyTo(env)
        }
    }

    override fun afterTestClass(context: TestContext) {
        mongoExecutable.stop()
    }

}
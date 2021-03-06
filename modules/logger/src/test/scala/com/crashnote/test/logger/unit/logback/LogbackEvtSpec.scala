/**
 * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crashnote.test.logger.unit.logback

import scala.collection.JavaConversions._
import com.crashnote.core.model.types.LogLevel
import com.crashnote.logback.impl.LogbackEvt
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi._
import com.crashnote.test.base.defs.BaseMockSpec

class LogbackEvtSpec
  extends BaseMockSpec[ILoggingEvent] {

  "Logback Event" should {

    "instantiate" >> {
      "example 1" >> {
        // mock
        val m_evt = getMock(Level.ERROR)

        m_evt.getThreadName returns "main"
        m_evt.getArgumentArray returns Array("Bob")
        m_evt.getLoggerName returns "com.example"
        m_evt.getTimeStamp returns 123456789L

        // execute
        val r = new LogbackEvt(m_evt, null)

        // verify
        r.getLoggerName === "com.example"
        r.getLevel === LogLevel.ERROR
        r.getArgs === Array("Bob")
        r.getTimeStamp returns 123456789L
        r.getThreadName === "main"
        r.getThrowable === null
      }

      "example 2" >> {
        val err = new RuntimeException("oops")

        // mock
        val m_evt = getMock(Level.WARN)

        m_evt.getFormattedMessage returns "oops"
        val m_thproxy = mock[ThrowableProxy]
        m_evt.getThrowableProxy returns m_thproxy
        m_thproxy.getThrowable returns err

        // execute
        val r = new LogbackEvt(m_evt, Map("test" -> "data"))

        // verify
        r.getThrowable === err
        r.getMessage === "oops"
        r.getLevel === LogLevel.WARN

        //r.copy()
        //r.getMDC.get("test") === "data"
      }
    }

    "convert log level" >> {
      "info" >> {
        new LogbackEvt(getMock(Level.INFO)).getLevel === LogLevel.INFO
      }
      "error" >> {
        new LogbackEvt(getMock(Level.ERROR)).getLevel === LogLevel.ERROR
      }
      "warn" >> {
        new LogbackEvt(getMock(Level.WARN)).getLevel === LogLevel.WARN
      }
      "debug" >> {
        new LogbackEvt(getMock(Level.DEBUG)).getLevel === LogLevel.DEBUG
        new LogbackEvt(getMock(Level.TRACE)).getLevel === LogLevel.DEBUG
      }
    }
  }

  // SETUP ======================================================================================

  def getMock(l: Level) = {
    val m_evt = mock[ILoggingEvent]
    m_evt.getLevel returns l
    m_evt
  }
}
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
package com.crashnote.test.web.unit.collect

import com.crashnote.test.base.defs.MockSpec
import com.crashnote.web.config.WebConfig
import com.crashnote.web.collect.SessionCollector
import com.crashnote.test.web.util.HTTPRequest
import com.crashnote.core.collect.BaseCollector

class SessionCollectorSpec
  extends MockSpec {

  "Session Collector" should {

    "inherit from BaseCollector" >> {
      val conf = mock[WebConfig]
      val sc = new SessionCollector[HTTPRequest](conf) {
        def collect(req: HTTPRequest) = null
      }
      sc must haveSuperclass[BaseCollector]
    }
  }
}
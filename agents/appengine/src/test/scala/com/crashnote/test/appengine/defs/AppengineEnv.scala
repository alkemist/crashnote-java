/**
 * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crashnote.test.appengine.defs

import com.crashnote.core.log.{LogLogFactory, LogLog}
import com.crashnote.appengine.config.AppengineConfig
import com.crashnote.test.base.defs.BaseMockSpec

trait AppengineEnv {

    self: BaseMockSpec[_] =>

    type C = AppengineConfig

    def mockConfig(): C = {
        val m_conf = mock[C]
        val lfact = new LogLogFactory(m_conf)
        m_conf.getLogger(anyClass) returns lfact.getLogger("")
        m_conf.getLogger(anyString) returns lfact.getLogger("")
        m_conf
    }
}
/**
 * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
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
package com.crashnote.test.base.defs

import org.specs2.mock.Mockito
import java.io.File

trait MockSpec
    extends UnitSpec with Mockito {


    // STUBBING ===================================================================================

    def anyFile = any[File]

    def anyClass = any[Class[_]]

    def anyObject = any[java.lang.Object]

    def anyThrowable = any[Throwable]

    protected def doReturn(toBeReturned: Any) =
        org.mockito.Mockito.doReturn(toBeReturned)

    protected def doThrow(toBeThrown: Throwable) =
        org.mockito.Mockito.doThrow(toBeThrown)

    protected def doThrow(toBeThrown: Class[_ <: Throwable]) =
        org.mockito.Mockito.doThrow(toBeThrown)


    // VERIFYING ==================================================================================

    def expect[S](s: => S) = there was s

    protected def verifyUntouched[S <: AnyRef](mocks: S*) {
        org.mockito.Mockito.verifyZeroInteractions(mocks: _*)
    }
}
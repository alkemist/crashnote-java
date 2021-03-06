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
package com.crashnote.test.base.defs

import java.lang.reflect.{Modifier, Field}
import com.crashnote.test.base.util.FactoryUtil

abstract class BaseMockSpec[T](implicit t: Manifest[T])
  extends MockSpec with FactoryUtil {

  var target: T = _


  // MOCKING ====================================================================================

  protected def _mock[F](implicit m: Manifest[F]): F =
    _set(mock(m))

  protected def _mock[F](fieldName: String)(implicit m: Manifest[F]): F =
    _mock[F](t.runtimeClass.getDeclaredField(fieldName))

  protected def _mock[F](field: Field)(implicit m: Manifest[F]): F =
    _set(field, mock(m))

  protected def reset[M](mocks: M*) {
    org.mockito.Mockito.reset(mocks: _*)
  }


  // REFLECTION =================================================================================

  protected def _set[F](value: F)(implicit m: Manifest[F]): F = {
    val clazz = t.runtimeClass
    val fields = findFields(clazz)
    for (fld <- fields)
      if (fld.getType.equals(m.runtimeClass))
        return _set(fld, value)
    for (fld <- fields)
      if (fld.getType.isAssignableFrom(m.runtimeClass))
        return _set(fld, value)

    sys.error("was unable to set field of type '" + m.runtimeClass + "' to '" + value + "'")
  }

  protected def _set[F](field: Field, value: F)(implicit m: Manifest[F]): F = {
    field.setAccessible(true)

    val modifier = classOf[Field].getDeclaredField("modifiers")
    modifier.setAccessible(true)
    modifier.setInt(field, field.getModifiers & ~Modifier.FINAL)

    field.set(target, value)

    value
  }

  @annotation.tailrec
  private def findFields(clazz: Class[_], acc: List[Field] = List()): List[Field] = {
    val fields = clazz.getDeclaredFields.toList ::: acc
    clazz.getSuperclass match {
      case null => fields
      case sc => findFields(sc, fields)
    }
  }
}
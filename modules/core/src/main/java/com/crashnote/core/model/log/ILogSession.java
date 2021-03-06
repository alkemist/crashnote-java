/**
 * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
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
package com.crashnote.core.model.log;

import java.util.List;
import java.util.Map;

/**
 * Interface of a log session which contains log events and log context.
 * It can either exist for a long time (e.g. client side) or a short time (e.g. server request).
 */
public interface ILogSession {

    void clear();

    ILogSession copy();


    // ==== EVENTS

    List<LogEvt<?>> getEvents();

    void addEvent(final LogEvt<?> evt);

    void clearEvents();

    boolean isEmpty();


    // ==== CONTEXT

    Map<String, Object> getContext();

    void putCtx(final String key, final Object val);

    void clearCtx();

    void removeCtx(final String key);

    public boolean hasContext();

}

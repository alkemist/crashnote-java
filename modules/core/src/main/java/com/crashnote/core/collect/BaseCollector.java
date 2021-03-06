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
package com.crashnote.core.collect;

import com.crashnote.core.build.Builder;
import com.crashnote.core.config.CrashConfig;
import com.crashnote.core.log.LogLog;
import com.crashnote.core.model.data.DataArray;
import com.crashnote.core.model.data.DataObject;
import com.crashnote.core.util.SystemUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Base class for sub-classes that want to save data into structured data objects and arrays.
 * It provides utility methods to create these containers by using the {@link Builder} class which
 * hides the concrete implementation details.
 */
public abstract class BaseCollector {

    // VARS =======================================================================================

    private final LogLog logger;
    private final Builder builder;
    private final SystemUtil sysUtil;

    private final DateFormat iso8601;
    protected final String filtered = "#";


    // SETUP ======================================================================================

    public <C extends CrashConfig> BaseCollector(final C config) {
        this.builder = config.getBuilder();
        this.logger = config.getLogger(this.getClass());
        this.sysUtil = config.getSystemUtil();

        iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // ISO 8601
        iso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    // SHARED =====================================================================================

    protected String formatTimestamp(final long timestamp) {
        return iso8601.format(new Date(timestamp));
    }

    protected DataObject createDataObj() {
        return builder.createDataObj();
    }

    protected DataArray createDataArr() {
        return builder.createDataArr();
    }

    protected DataArray createDataArr(final Object[] values) {
        final DataArray arr = createDataArr();
        {
            Collections.addAll(arr, values);
        }
        return arr;
    }

    protected <T extends Object> DataArray createDataArr(final List<T> values) {
        final DataArray arr = createDataArr();
        {
            for (final Object v : values) arr.add(v);
        }
        return arr;
    }

    protected SystemUtil getSysUtil() {
        return sysUtil;
    }


    // GET ========================================================================================

    LogLog getLogger() {
        return logger;
    }
}

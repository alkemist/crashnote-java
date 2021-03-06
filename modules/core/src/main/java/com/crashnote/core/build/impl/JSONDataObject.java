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
package com.crashnote.core.build.impl;

import com.crashnote.core.model.data.DataArray;
import com.crashnote.core.model.data.DataObject;
import com.crashnote.external.json.JSONArray;
import com.crashnote.external.json.JSONObject;

import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of a {@link DataObject}, unifies the interface with the functionality of a
 * JSON Object implementation (see {@link JSONObject}).
 */
public class JSONDataObject
    extends JSONObject implements DataObject {

    private static final long serialVersionUID = 1L;

    // SETUP ======================================================================================

    public JSONDataObject() {
        super();
    }


    // INTERFACE ==================================================================================

    @Override
    public void streamTo(final Writer out) throws IOException {
        writeJSONString(out);
    }

    @Override
    public void appendTo(final String key, final Object data) {
        if (!containsKey(key)) put(key, new JSONDataArray());
        final JSONArray arr = (JSONArray) get(key);
        arr.add(data);
    }

    @Override
    public Object put(final String key, final Object value) {
        if (value == null) return null;
        else return super.put(key, value);
    }

    @Override
    public Object putArr(final String key, final DataArray value) {
        return (value == null || value.isEmpty()) ? null : super.put(key, value);
    }

    @Override
    public Object putObj(final String key, final DataObject value) {
        return (value == null || value.isEmpty()) ? null : super.put(key, value);
    }
}

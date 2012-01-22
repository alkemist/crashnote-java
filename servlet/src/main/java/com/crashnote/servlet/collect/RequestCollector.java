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
package com.crashnote.servlet.collect;

import com.crashnote.core.collect.BaseCollector;
import com.crashnote.core.config.IConfigChangeListener;
import com.crashnote.core.model.data.DataObject;
import com.crashnote.core.util.ChksumUtil;
import com.crashnote.servlet.config.ServletConfig;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

import static com.crashnote.core.util.FilterUtil.doFilter;

/**
 * Collector to transform a HTTP request into a structured data format.
 */
public class RequestCollector
    extends BaseCollector<ServletConfig> implements IConfigChangeListener<ServletConfig> {

    protected String[] requestFilters;
    protected boolean skipHeaderData;

    // SETUP ======================================================================================

    public RequestCollector(final ServletConfig config) {
        super(config);
        updateConfig(config);
    }

    public void updateConfig(final ServletConfig config) {
        config.addListener(this);
        this.requestFilters = config.getRequestFilters();
        this.skipHeaderData = config.getSkipHeaderData();
    }

    // INTERFACE ==================================================================================

    public DataObject collect(final HttpServletRequest req) {
        final DataObject data = collectReqBase(req);
        {
            data.putObj("param", collectReqParams(req));
            if (!skipHeaderData) data.putObj("header", collectReqHeader(req));

        }
        return data;
    }

    // SHARED =====================================================================================

    protected DataObject collectReqBase(final HttpServletRequest req) {
        final DataObject data = createDataObj();
        {
            data.put("mth", req.getMethod());
            data.put("url", req.getRequestURL().toString());

            final String userIP = req.getRemoteAddr();
            data.put("iph", ChksumUtil.hash(userIP));

            final Principal principal = req.getUserPrincipal();
            if (principal != null) data.put("principal", principal.getName());
        }
        return data;
    }

    protected DataObject collectReqHeader(final HttpServletRequest req) {
        final DataObject data = createDataObj();
        {
            final Enumeration names = req.getHeaderNames();
            while (names.hasMoreElements()) {
                final String name = names.nextElement().toString();

                final List<String> values = new ArrayList<String>();
                final Enumeration header = req.getHeaders(name);
                while (header.hasMoreElements()) {
                    final String value = header.nextElement().toString();
                    values.add(value);
                }
                if (values.size() == 1) data.put(name, values.get(0));
                else data.put(name, createDataArr(values));
            }
        }
        return data;
    }

    protected DataObject collectReqParams(final HttpServletRequest req) {
        final DataObject data = createDataObj();
        {
            final Enumeration names = req.getParameterNames();
            while (names.hasMoreElements()) {
                final String name = names.nextElement().toString();
                data.put(name, doFilter(name, requestFilters) ? "#" : req.getParameter(name));
            }
        }
        return data;
    }

}

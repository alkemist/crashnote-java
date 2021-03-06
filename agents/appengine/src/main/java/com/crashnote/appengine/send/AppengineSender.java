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
package com.crashnote.appengine.send;

import com.crashnote.appengine.config.AppengineConfig;
import com.crashnote.appengine.util.AppengineUtil;
import com.crashnote.core.model.log.LogReport;
import com.crashnote.core.send.Sender;
import com.google.appengine.api.urlfetch.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import static com.google.appengine.api.urlfetch.FetchOptions.Builder.allowTruncate;

/**
 * Customized {@link Sender} that uses the AppEngine API to send the data via HTTP POST
 * because of the restrictions of the platform. The advantage is that this happens asynchronous
 * and doesn't slow down the application (since regular threads are not allowed).
 */
public class AppengineSender
    extends Sender {

    // VARS =======================================================================================

    private final AppengineUtil appengineUtil;


    // SETUP ======================================================================================

    public <C extends AppengineConfig> AppengineSender(final C config) {
        super(config);
        appengineUtil = new AppengineUtil();
    }


    // SHARED =====================================================================================

    @Override
    protected void POST(final String url, final LogReport report) {
        try {
            // prepare request
            final HTTPRequest req = prepareRequest(url);
            final byte[] data = report.toString().getBytes("UTF-8");
            req.setPayload(data);

            // execute request
            appengineUtil.execRequest(req, true);

        } catch (IOException e) {
            logger.debug("unable to send data", e);
        }
    }

    // INTERNALS =====================================================================================

    private HTTPRequest prepareRequest(final String url) throws IOException {
        final FetchOptions fetchOptions =
            allowTruncate().doNotValidateCertificate().followRedirects()
                .setDeadline((double) getConnectionTimeout());

        final HTTPRequest req = appengineUtil.createRequest(url, HTTPMethod.POST, fetchOptions);
        {
            if (getClientInfo() != null)
                req.setHeader(new HTTPHeader("User-Agent", getClientInfo()));


        }
        return req;
    }

    @Override
    protected void installCustomTrustManager() {
        // do nothing
    }

}

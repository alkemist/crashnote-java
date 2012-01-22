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
package com.crashnote.core.config;

import com.crashnote.core.log.LogLog;
import com.crashnote.core.util.SystemUtil;

import java.util.*;

import static com.crashnote.core.config.Config.*;

/**
 * This class is responsible for instantiating a {@link Config} object and internally apply
 * the configuration settings (e.g. from system props, property file).
 * <p/>
 * Finally it validates the config ("fail fast") and returns it.
 */
public class ConfigFactory<C extends Config> {

    protected final C config;
    private final LogLog logger;
    private final SystemUtil sysUtil;

    /**
     * specifies whether the config object was already validated
     */
    private boolean validated = false;

    /**
     * indicates whether the external configuration sources are already processed
     */
    private boolean extConfigLoaded = false;

    /**
     * name of the configuration file to parse for settings
     */
    public static final String PROP_FILE = Config.LIB_NAME + ".properties";

    // SETUP ======================================================================================

    public ConfigFactory() {
        this((C) new Config());
    }

    protected ConfigFactory(final C config) {
        this.config = config;
        this.sysUtil = new SystemUtil();
        this.logger = config.getLogger(this.getClass());
    }

    // INTERFACE ==================================================================================

    public C get() {
        if (!validated) {
            prepare();
            validate(); // validate first to fail fast if necessary
            validated = true;
        }
        return config;
    }

    // SHARED =====================================================================================

    protected void prepare() {
        loadExt(); // load external configuration (e.g. property file)
    }

    protected void applyProperties(final Properties props, final boolean strict) {
        config.setClientInfo(getProperty(props, PROP_CLIENT, strict));
        setKey(getProperty(props, PROP_API_KEY, strict));
        setDebug(getProperty(props, PROP_DEBUG, strict));
        setSync(getProperty(props, PROP_SYNC, strict));
        setEnabled(getProperty(props, PROP_ENABLED, strict));
        setHost(getProperty(props, PROP_API_HOST, strict));
        setMode(getProperty(props, PROP_APP_MODE, strict));
        setSecure(getProperty(props, PROP_API_SECURE, strict));
        setPort(getProperty(props, PROP_API_PORT, strict));
        setSslPort(getProperty(props, PROP_API_PORT_SSL, strict));
        setAppVersion(getProperty(props, PROP_APP_VERSION, strict));
        setConnectionTimeout(getProperty(props, PROP_API_TIMEOUT, strict));
    }

    protected String getProperty(final Properties props, final String name, final boolean strict) {
        if (props == null) return null;
        final Enumeration<?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            final Object propName = propNames.nextElement();
            if (propName instanceof String) {
                final String key = (String) propName;
                if (strict) {
                    if (key.equalsIgnoreCase(getConfigKey(name))
                        || key.equalsIgnoreCase(getConfigKey(name, '_'))
                        || key.equalsIgnoreCase(getConfigKey(name, '-')))
                        return props.getProperty(key);
                } else if (key.equalsIgnoreCase(name))
                    return props.getProperty(key);
            }
        }
        return null;
    }

    protected void loadExt() {
        if (!extConfigLoaded) {
            loadFileProperties();       // #1: load file props
            loadEnvProperties();        // #2: load environment props
            loadSystemProperties();     // #3: load system props
            extConfigLoaded = true;
        }
    }

    // INTERNALS ==================================================================================

    private void loadSystemProperties() {
        applyProperties(sysUtil.getProperties(), true);
    }

    private void loadEnvProperties() {
        applyProperties(sysUtil.getEnvProperties(), true);
    }

    private void loadFileProperties() {
        applyProperties(sysUtil.loadProperties(PROP_FILE), false);
    }

    private void validate() {
        if (isEnabled())
            validateKey();
        else
            logger.info("transfer to Cloud is DISABLED");
    }

    private boolean isEnabled() {
        if (config.isEnabled()) {
            logger.info("Status: ON");
            return true;
        } else {
            logger.info("Status: OFF");
            return false;
        }
    }

    private void validateKey() {
        final String key = config.getKey();
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException(
                "The API Key is missing, please login to the web app under [" + LIB_URL_BOARD + "], " +
                    "browse to your app and consult the 'Install' instructions.");

        } else if (key.length() != 36 || key.charAt(8) != '-' || key.charAt(13) != '-' || key.charAt(18) != '-' || key.charAt(23) != '-')
            throw new IllegalArgumentException(
                "The API Key appears to be invalid (it should be 36 characters long with 4 dashes), " +
                    "please login to the web app under [" + LIB_URL_BOARD + "], " +
                    "browse to your app and consult the 'Install' instructions.");
    }

    // SET ========================================================================================

    public void setEnabled(final String enabled) {
        config.setEnabled(enabled);
    }

    public void setDebug(final String debug) {
        config.setDebug(debug);
    }

    public void setSslPort(final String sslPort) {
        config.setSSLPort(sslPort);
    }

    public void setSecure(final String secure) {
        config.setSecure(secure);
    }

    public void setSync(final String sync) {
        config.setSync(sync);
    }

    public void setPort(final String port) {
        config.setPort(port);
    }

    public void setMode(final String mode) {
        config.setAppMode(mode);
    }

    public void setAppVersion(final String appVersion) {
        config.setAppVersion(appVersion);
    }

    public void setSecure(final Boolean secure) {
        config.setSecure(secure);
    }

    public void setConnectionTimeout(final String timeout) {
        config.setConnectionTimeout(timeout);
    }

    public void setKey(final String key) {
        config.setKey(key);
    }

    public void setEnabled(final boolean enabled) {
        config.setEnabled(enabled);
    }

    public void setPort(final int port) {
        config.setPort(port);
    }

    public void setSslPort(final int sslPort) {
        config.setSSLPort(sslPort);
    }

    public void setHost(final String host) {
        config.setHost(host);
    }

    public void setDebug(final Boolean debug) {
        config.setDebug(debug);
    }

    // GET ========================================================================================

    public LogLog getLogger() {
        return logger;
    }
}

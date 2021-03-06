package com.crashnote.play2.config;

import com.crashnote.external.config.Config;
import com.crashnote.external.config.ConfigFactory;
import com.crashnote.logger.config.LoggerConfigFactory;
import play.Application;
import play.Configuration;

/**
 * Factory to create instance(s) of {@link Play2Config}.
 * The user's configuration is provided through Play's own config file, not a separate one.
 */
public class Play2ConfigFactory
    extends LoggerConfigFactory<Play2Config> {

    // VARS =======================================================================================

    private Application application;


    // SETUP ======================================================================================

    public Play2ConfigFactory(final Application app) {
        super();
        this.application = app;
    }


    // SHARED =====================================================================================

    @Override
    protected Play2Config create() {
        return new Play2Config(readConf());
    }

    @Override
    protected Config readUserFileConf() {
        if (application != null) {
            final Configuration playConf = application.configuration();
            final String confPath = playConf.getWrappedConfiguration().underlying().origin().resource();
            if (confPath != null)
                return loader.fromFile(confPath);
        }
        return ConfigFactory.empty();
    }
}

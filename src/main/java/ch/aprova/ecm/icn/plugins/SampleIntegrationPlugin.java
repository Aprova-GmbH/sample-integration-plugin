package ch.aprova.ecm.icn.plugins;

import ch.aprova.ecm.icn.plugins.services.DossierDetailsService;
import com.ibm.ecm.extension.*;

import java.util.Locale;

public class SampleIntegrationPlugin extends Plugin {

    public static final String ID = "SampleIntegrationPlugin";

    @Override
    public PluginAction[] getActions() {
        return new PluginAction[]{};
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName(Locale locale) {
        return "Aprova sample integration plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public PluginService[] getServices() {
        return new PluginService[] {
                new DossierDetailsService()
        };
    }

    @Override
    public PluginMenu[] getMenus() {
        return new PluginMenu[] { };
    }

    @Override
    public String getScript() {
        return null;
    }

    @Override
    public String getDebugScript() {
        return null;
    }

    @Override
    public String getDojoModule() {
        return null;
    }

    @Override
    public String[] getCSSFileNames() {
        return new String[] { };
    }

    @Override
    public PluginFeature[] getFeatures() {
        return new PluginFeature[] { };
    }

    @Override
    public PluginResponseFilter[] getResponseFilters() {
        return new PluginResponseFilter[] { };
    }

    @Override
    public String getConfigurationDijitClass() {
        return null;
    }
}

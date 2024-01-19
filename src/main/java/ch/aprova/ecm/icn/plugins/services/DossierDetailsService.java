package ch.aprova.ecm.icn.plugins.services;

import ch.aprova.ecm.icn.utils.PluginResponseUtil;
import com.ibm.ecm.extension.PluginService;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.json.java.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Properties;

public class DossierDetailsService extends PluginService {

    public static final String DOSSIER_NR = "dossierNr";

    @Override
    public String getId() {
        return "dossierDetailsService";
    }

    @Override
    public void execute(PluginServiceCallbacks callbacks,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {

        // get file number from http request
        String dossierNr = request.getParameter(DOSSIER_NR);

        System.out.println("DossierNr: " + dossierNr);
        callbacks.getLogger().logInfo(DossierDetailsService.class, "search", "Get Dossier details for " + dossierNr);

        // Response object
        JSONObject responseMessage = new JSONObject();
        Properties props = new Properties();

        // get demo file information's. this part should be replaced by your own code
        InputStream io = DossierDetailsService.class.getResourceAsStream("/demo/dossierDetails-" + dossierNr + ".properties");
        if (io == null) {
            io = DossierDetailsService.class.getResourceAsStream("/demo/dossierDetails.properties");
        }

        // write file information as key value pairs
        try{
            props.load(io);
            props.forEach((key, value) -> {
                if (key != null) {
                    responseMessage.put(key, value);
                }
            });
        } finally {
            io.close();
        }

        PluginResponseUtil.writeJSONResponse(request, response, responseMessage, callbacks, getId());
    }
}

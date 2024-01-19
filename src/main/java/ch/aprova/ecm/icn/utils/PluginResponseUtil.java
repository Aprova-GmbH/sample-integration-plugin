package ch.aprova.ecm.icn.utils;


import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.ecm.json.JSONMessage;
import com.ibm.ecm.json.JSONResponse;
import com.ibm.ecm.mediator.BaseMediator;
import com.ibm.ecm.util.PluginUtil;
import com.ibm.json.java.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class PluginResponseUtil {
    private static final String SECURE_JSON_PREFIX = "{}&&";

    public PluginResponseUtil() {
    }

    public static boolean writeJSONResponse(HttpServletRequest request, HttpServletResponse response, JSONObject jsonResponse, PluginServiceCallbacks callbacks, String pluginServiceId) {
        String methodName = "writeJSONMediator";
        callbacks.getLogger().logEntry(PluginResponseUtil.class, methodName, request);
        boolean secureJsonPrefixWritten = false;
        boolean returnVal = false;
        try {
            response.addHeader("Cache-Control", "no-cache, no-store");
            BaseMediator.updateSecurityToken(jsonResponse, request);
            String securityToken;
            if (jsonResponse.containsKey("security_token")) {
                callbacks.getLogger().logDebug(PluginResponseUtil.class, methodName, request, "Getting security token from json.");
                securityToken = (String)jsonResponse.get("security_token");
            } else {
                callbacks.getLogger().logDebug(PluginResponseUtil.class, methodName, request, "Setting security token from header.");
                securityToken = request.getHeader("security_token");
            }

            if (securityToken != null && !securityToken.isEmpty()) {
                response.addHeader("Set-Cookie", "ECM-XSRF-Token=" + securityToken + "; Path=/; HttpOnly");
            }

            if (PluginUtil.responseFilterExists(request, pluginServiceId)) {
                callbacks.getLogger().logDebug(PluginResponseUtil.class, methodName, request, "Response filters were found.");

                try {
                    PluginUtil.invokeResponseFilters(request, jsonResponse, pluginServiceId);
                } catch (Exception ex) {

                    callbacks.getLogger().logError(PluginResponseUtil.class, methodName, request, ex);
                }

                writeJSON(request, response, jsonResponse, callbacks);
                returnVal = true;
            } else {
                writeJSON(request, response, jsonResponse, callbacks);
                returnVal = true;
            }
        } catch (UnsupportedEncodingException ex) {
            callbacks.getLogger().logError(PluginResponseUtil.class, methodName, request, ex);
        } catch (Throwable ex) {
            callbacks.getLogger().logError(PluginResponseUtil.class, methodName, request, ex);
            JSONResponse json = new JSONResponse();
            String msgText = callbacks.getMessageResources().getMessage(request.getLocale(), "error.exception.general");
            String msgId = callbacks.getMessageResources().getMessage(request.getLocale(), "error.exception.general.id");
            int msgNumber = 0;
            if (msgId != null) {
                msgNumber = Integer.parseInt(msgId.trim());
            }

            String msgExplanation = callbacks.getMessageResources().getMessage(request.getLocale(), "error.exception.general.explanation");
            String msgUserResponse = callbacks.getMessageResources().getMessage(request.getLocale(), "error.exception.general.userResponse");
            String msgAdminResponse = callbacks.getMessageResources().getMessage(request.getLocale(), "error.exception.general.adminResponse");
            JSONMessage message = new JSONMessage(msgNumber, msgText, msgExplanation, msgUserResponse, msgAdminResponse, null);
            json.put("error", message);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

            try {
                Writer writer = response.getWriter();
                if (isSecureServiceEnabled(request) && !secureJsonPrefixWritten) {
                    writer.write(SECURE_JSON_PREFIX);
                }

                writer.write(json.toString());
                writer.flush();
                writer.close();
                returnVal = true;
            } catch (Exception var17) {
                callbacks.getLogger().logError(com.ibm.ecm.extension.PluginResponseUtil.class, methodName, request, var17);
            }
        }

        callbacks.getLogger().logExit(com.ibm.ecm.extension.PluginResponseUtil.class, methodName, request);
        return returnVal;
    }

    private static boolean writeJSON(HttpServletRequest request, HttpServletResponse response, JSONObject jsonResponse, PluginServiceCallbacks callbacks) throws Exception {
        String methodName = "writeJSON";
        boolean returnVal = false;
        String acceptedEncodings = request.getHeader("Accept-Encoding");
        if (acceptedEncodings != null && acceptedEncodings.contains("gzip")) {
            callbacks.getLogger().logDebug(com.ibm.ecm.extension.PluginResponseUtil.class, methodName, request, "GZIP is supported, sending compressed response.");
            if (!response.isCommitted()) {
                response.setBufferSize(65536);
            }

            response.setHeader("Content-Encoding", "gzip");
            response.setContentType("text/plain");
            GZIPOutputStream gzos = new GZIPOutputStream(response.getOutputStream());
            Writer writer = new OutputStreamWriter(gzos, StandardCharsets.UTF_8);
            if (isSecureServiceEnabled(request)) {
                writer.write(SECURE_JSON_PREFIX);
                returnVal = true;
            }

            callbacks.getLogger().logDebug(com.ibm.ecm.extension.PluginResponseUtil.class, methodName, request, "GZIP: Calling to write JSON response.");
            writer.write(jsonResponse.toString());
            writer.flush();
            writer.close();
        } else {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            Writer writer = response.getWriter();
            if (isSecureServiceEnabled(request)) {
                writer.write(SECURE_JSON_PREFIX);
                returnVal = true;
            }

            callbacks.getLogger().logDebug(com.ibm.ecm.extension.PluginResponseUtil.class, methodName, request, "Uncompressed: Calling to write JSON response.");
            writer.write(jsonResponse.toString());
            writer.flush();
            writer.close();
        }

        return returnVal;
    }

    private static boolean isSecureServiceEnabled(HttpServletRequest request) {
        return request != null && request.getAttribute("enableSecureService") == Boolean.TRUE;
    }
}

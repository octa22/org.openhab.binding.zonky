/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zonky.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.zonky.ZonkyBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 *
 * @author Ondrej Pecta
 * @since 1.9.0
 */
public class ZonkyBinding extends AbstractActiveBinding<ZonkyBindingProvider> {

    private static final Logger logger =
            LoggerFactory.getLogger(ZonkyBinding.class);

    //constants
    private final String ZONKY_URL = "https://api.zonky.cz/";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36";

    /**
     * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
     * method and must not be accessed anymore once the deactivate() method was called or before activate()
     * was called.
     */
    private BundleContext bundleContext;


    /**
     * the refresh interval which is used to poll values from the Zonky
     * server (optional, defaults to 60000ms)
     */
    private long refreshInterval = 240000;

    private String userName = "";
    private String password = "";
    private String token = "";
    private String refreshToken = "";

    //Gson parser
    private JsonParser parser = new JsonParser();

    public ZonkyBinding() {
    }


    /**
     * Called by the SCR to activate the component with its configuration read from CAS
     *
     * @param bundleContext BundleContext of the Bundle that defines this component
     * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
     */
    public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
        this.bundleContext = bundleContext;

        // the configuration is guaranteed not to be null, because the component definition has the
        // configuration-policy set to require. If set to 'optional' then the configuration may be null

        readConfiguration(configuration);

        setProperlyConfigured(true);
    }

    private void readConfiguration(Map<String, Object> configuration) {
        String refreshIntervalString = (String) configuration.get("refresh");
        if (StringUtils.isNotBlank(refreshIntervalString)) {
            refreshInterval = Long.parseLong(refreshIntervalString);
        }

        String userNameString = (String) configuration.get("username");
        if (StringUtils.isNotBlank(userNameString)) {
            userName = userNameString;
        }

        String passwordString = (String) configuration.get("password");
        if (StringUtils.isNotBlank(passwordString)) {
            password = passwordString;
        }

    }

    /**
     * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
     *
     * @param configuration Updated configuration properties
     */
    public void modified(final Map<String, Object> configuration) {
        // update the internal configuration accordingly
    }

    /**
     * Called by the SCR to deactivate the component when either the configuration is removed or
     * mandatory references are no longer satisfied or the component has simply been stopped.
     *
     * @param reason Reason code for the deactivation:<br>
     *               <ul>
     *               <li> 0 – Unspecified
     *               <li> 1 – The component was disabled
     *               <li> 2 – A reference became unsatisfied
     *               <li> 3 – A configuration was changed
     *               <li> 4 – A configuration was deleted
     *               <li> 5 – The component was disposed
     *               <li> 6 – The bundle was stopped
     *               </ul>
     */
    public void deactivate(final int reason) {
        this.bundleContext = null;
        // deallocate resources here that are no longer needed and
        // should be reset when activating this binding again
        logout();
    }


    /**
     * @{inheritDoc}
     */
    @Override
    protected long getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected String getName() {
        return "Zonky Refresh Service";
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected void execute() {
        // the frequently executed code (polling) goes here ...
        if (!bindingsExist()) {
            return;
        }
        //first call
        if (token.equals("")) {
            login();
        } else {
            if (!refreshToken()) {
                token = "";
                login();
            }
        }

        String wallet = getWallet();
        if (wallet == null) {
            return;
        }
        JsonObject jobject = parser.parse(wallet).getAsJsonObject();
        if (jobject != null) {
            //&& jobject.has("balance")
            for (final ZonkyBindingProvider provider : providers) {
                for (String itemName : provider.getItemNames()) {
                    String type = provider.getItemType(itemName);
                    if (jobject.has(type)) {
                        Number value = jobject.get(type).getAsBigDecimal();
                        eventPublisher.postUpdate(itemName, new StringType(value.toString() + " CZK"));
                    }
                }
            }
        }

    }

    private Boolean refreshToken() {
        String url = null;

        try {
            //login
            url = ZONKY_URL + "oauth/token";
            String urlParameters = "refresh_token=" + refreshToken + "&grant_type=refresh_token&scope=SCOPE_APP_WEB";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            setupConnectionDefaults(connection);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("Authorization", "Basic d2ViOndlYg==");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            String line = readResponse(connection);
            logger.debug("Response: " + line);
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject != null) {
                token = jobject.get("access_token").getAsString();
                refreshToken = jobject.get("refresh_token").getAsString();
                return true;
            }

        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky login token: " + e.toString());
        }
        return false;
    }

    private String getWallet() {
        String url = null;

        try {
            //login
            url = ZONKY_URL + "users/me/wallet";

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            setupConnectionDefaults(connection);
            connection.setRequestProperty("Authorization", "Bearer " + token);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            String line = readResponse(connection);
            logger.debug("Response: " + line);
            return line;
        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky wallet response: " + e.toString());
        }
        return null;
    }

    private String logout() {
        String url = null;

        try {
            //login
            url = ZONKY_URL + "users/me/logout";

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            setupConnectionDefaults(connection);
            connection.setRequestProperty("Authorization", "Bearer " + token);

            String line = readResponse(connection);
            logger.debug("Response: " + line);
            return line;
        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky logout response: " + e.toString());
        }
        return null;
    }

    private void login() {
        String url = null;

        try {
            //login
            url = ZONKY_URL + "oauth/token";
            String urlParameters = "username=" + userName + "&password=" + password + "&grant_type=password&scope=SCOPE_APP_WEB";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            setupConnectionDefaults(connection);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("Authorization", "Basic d2ViOndlYg==");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            String line = readResponse(connection);
            logger.debug("Response: " + line);
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject != null) {
                token = jobject.get("access_token").getAsString();
                refreshToken = jobject.get("refresh_token").getAsString();
                if (!token.equals("")) {
                    logger.info("Successfully logged in to Zonky!");
                }
            }

        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky login token: " + e.toString());
        }
    }

    private void setupConnectionDefaults(HttpsURLConnection connection) {
        //connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "cs-CZ");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Referer", "https://app.zonky.cz/");
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
    }

    private String readResponse(HttpsURLConnection connection) throws Exception {
        InputStream stream = connection.getInputStream();
        String line;
        StringBuilder body = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }
        line = body.toString();
        logger.debug(line);
        return line;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected void internalReceiveCommand(String itemName, Command command) {
        // the code being executed when a command was sent on the openHAB
        // event bus goes here. This method is only called if one of the
        // BindingProviders provide a binding for the given 'itemName'.
        logger.debug("internalReceiveCommand({},{}) is called!", itemName, command);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected void internalReceiveUpdate(String itemName, State newState) {
        // the code being executed when a state was sent on the openHAB
        // event bus goes here. This method is only called if one of the
        // BindingProviders provide a binding for the given 'itemName'.
        logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);
    }

}

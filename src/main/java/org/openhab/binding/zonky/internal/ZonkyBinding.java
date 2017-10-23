/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zonky.internal;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.zonky.ZonkyBindingProvider;
import org.openhab.binding.zonky.internal.model.*;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
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
    private ItemRegistry itemRegistry;


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
    private Gson gson = new Gson();

    public ZonkyBinding() {
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
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
        setProperlyConfigured(!userName.isEmpty() && !password.isEmpty());
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
        if (configuration != null) {
            readConfiguration(configuration);
            setProperlyConfigured(!userName.isEmpty() && !password.isEmpty());
        }
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
        if (!token.isEmpty()) {
            logout();
        }
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
        if (token.isEmpty()) {
            login();
        } else {
            if (!refreshToken()) {
                token = "";
                login();
            }
        }

        if (token.isEmpty())
            return;

        String wallet = getWallet();
        String statistics = getStatistics();
        String weekly = getWeeklyStatistics();
        if (wallet == null || statistics == null || weekly == null) {
            return;
        }

        ZonkyWalletResponse walletResponse = gson.fromJson(wallet, ZonkyWalletResponse.class);
        ZonkyStatResponse statResponse = gson.fromJson(statistics, ZonkyStatResponse.class);
        ZonkyWeeklyStatResponse weeklyResponse = gson.fromJson(weekly, ZonkyWeeklyStatResponse.class);
        State oldValue = null;

        for (final ZonkyBindingProvider provider : providers) {
            for (String itemName : provider.getItemNames()) {
                String type = provider.getItemType(itemName);
                State newValue = null;
                try {
                    oldValue = itemRegistry.getItem(itemName).getState();
                    if (isWalletType(type)) {
                        Number value = getWalletValue(walletResponse, type);
                        newValue = new StringType(value.toString());
                    } else if (isWeeklyStatiscticsType(type)) {
                        type = type.replace("weekly.", "");
                        Number value = getWeeklyStatValue(weeklyResponse, type);
                        newValue = new DecimalType(value.doubleValue());
                    } else {
                        if (isPercentType(type)) {
                            Double value = type.equals("currentProfitability") ? statResponse.getCurrentProfitability().doubleValue() : statResponse.getExpectedProfitability().doubleValue();
                            newValue = new DecimalType(value * 100);
                        }
                        if (newValue == null) {
                            ZonkyCurrentOverview currentOverview = statResponse.getCurrentOverview();
                            ZonkyOverallOverview overallOverview = statResponse.getOverallOverview();

                            String subType;
                            if (type.startsWith("currentOverview.")) {
                                subType = type.replace("currentOverview.", "");
                                Number value = GetStatCurrentOverviewValue(currentOverview, subType);
                                newValue = new StringType(value.toString());
                            } else if (type.startsWith("overallOverview.")) {
                                subType = type.replace("overallOverview.", "");
                                Number value = GetStatOverallOverviewValue(overallOverview, subType);
                                newValue = new StringType(value.toString());
                            }
                        }
                    }
                    if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                        eventPublisher.postUpdate(itemName, newValue);
                    }
                } catch (ItemNotFoundException e) {
                    logger.error(e.toString());
                }
            }
        }
    }

    private Number getWeeklyStatValue(ZonkyWeeklyStatResponse weekly, String type) {
        switch (type) {
            case "newInvestments":
                return weekly.getNewInvestments();
            case "newInvestmentsAmount":
                return weekly.getNewInvestmentsAmount();
            case "paidInstalments":
                return weekly.getPaidInstalments();
            case "paidInstalmentsAmount":
                return weekly.getPaidInstalmentsAmount();
            case "soldInvestments":
                return weekly.getSoldInvestments();
            case "soldInvestmentsAmount":
                return weekly.getSoldInvestmentsAmount();
            case "boughtInvestments":
                return weekly.getBoughtInvestments();
            case "boughtInvestmentsAmount":
                return weekly.getBoughtInvestmentsAmount();
        }
        return 0;
    }

    private boolean isIntegerType(String type) {
        return type.equals("newInvestments") || type.equals("paidInstalments") || type.equals("soldInvestments") || type.equals("boughtInvestments");
    }

    private Number GetStatOverallOverviewValue(ZonkyOverallOverview overallOverview, String subType) {
        switch (subType) {
            case "feesAmount":
                return overallOverview.getFeesAmount();
            case "interestPaid":
                return overallOverview.getInterestPaid();
            case "investmentCount":
                return overallOverview.getInvestmentCount();
            case "netIncome":
                return overallOverview.getNetIncome();
            case "principalLost":
                return overallOverview.getPrincipalLost();
            case "principalPaid":
                return overallOverview.getPrincipalPaid();
            case "totalInvestment":
                return overallOverview.getTotalInvestment();
        }
        return 0;
    }

    private Number GetStatCurrentOverviewValue(ZonkyCurrentOverview currentOverview, String subType) {
        switch (subType) {
            case "interestLeft":
                return currentOverview.getInterestLeft();
            case "interestLeftDue":
                return currentOverview.getInterestLeftDue();
            case "interestLeftToPay":
                return currentOverview.getInterestLeftToPay();
            case "interestPaid":
                return currentOverview.getInterestPaid();
            case "interestPanned":
                return currentOverview.getInterestPlanned();
            case "investmentCount":
                return currentOverview.getInvestmentCount();
            case "principalLeft":
                return currentOverview.getPrincipalLeft();
            case "principalLeftDue":
                return currentOverview.getPrincipalLeftDue();
            case "principalLeftToPay":
                return currentOverview.getPrincipalLeftToPay();
            case "principalPaid":
                return currentOverview.getPrincipalPaid();
            case "totalInvestment":
                return currentOverview.getTotalInvestment();
        }

        return 0;
    }

    private Number getWalletValue(ZonkyWalletResponse wallet, String type) {
        switch (type) {
            case "balance":
                return wallet.getBalance();
            case "availableBalance":
                return wallet.getAvailableBalance();
            case "blockedBalance":
                return wallet.getBlockedBalance();
            case "creditSum":
                return wallet.getCreditSum();
            case "debitSum":
                return wallet.getDebitSum();
        }
        return 0;
    }

    private boolean isPercentType(String type) {
        return type.equals("currentProfitability") ||
                type.equals("expectedProfitability");
    }

    private boolean isWalletType(String type) {
        return type.equals("balance") ||
                type.equals("availableBalance") ||
                type.equals("blockedBalance") ||
                type.equals("creditSum") ||
                type.equals("debitSum");
    }

    private boolean isWeeklyStatiscticsType(String type) {
        return type.startsWith("weekly.");
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
            /*
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject != null) {
                token = jobject.get("access_token").getAsString();
                refreshToken = jobject.get("refresh_token").getAsString();
                return true;
            }*/
            ZonkyTokenResponse response = gson.fromJson(line, ZonkyTokenResponse.class);
            token = response.getAccessToken();
            refreshToken = response.getRefreshToken();
            return true;

        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky login token: " + e.toString());
        }
        return false;
    }

    private String getWallet() {
        return sendJsonRequest("users/me/wallet");
    }

    private String getStatistics() {
        return sendJsonRequest("users/me/investments/statistics");
    }

    private String getWeeklyStatistics() {
        return sendJsonRequest("users/me/investments/weekly-statistics");
    }


    private String sendJsonRequest(String uri) {
        String url = null;

        try {
            //login
            url = ZONKY_URL + uri;

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

            ZonkyTokenResponse response = gson.fromJson(line, ZonkyTokenResponse.class);
            token = response.getAccessToken();
            refreshToken = response.getRefreshToken();
            if (!token.isEmpty()) {
                logger.info("Successfully logged in to Zonky!");
            }
        } catch (MalformedURLException e) {
            logger.error("The URL '" + url + "' is malformed: " + e.toString());
        } catch (Exception e) {
            logger.error("Cannot get Zonky login token: " + e.toString());
        }
    }

    private void setupConnectionDefaults(HttpsURLConnection connection) {
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "cs-CZ");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept", "application/json, text/plain, */*");
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

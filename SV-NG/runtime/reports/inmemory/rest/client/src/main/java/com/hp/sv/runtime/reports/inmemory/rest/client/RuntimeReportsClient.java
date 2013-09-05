package com.hp.sv.runtime.reports.inmemory.rest.client;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

public class RuntimeReportsClient implements com.hp.sv.runtime.reports.api.RuntimeReportsClient {
    private static final Log logger = LogFactory.getLog(RuntimeReportsClient.class);

    private static final String VsId = "vsId";
    private static final String Count = "count";

    private final URI serverUri;
    private static final String runtimeReportUrl = "/runtime-report";
    private static final String runtimeReportWithIdUrl = runtimeReportUrl + "/{id}";
    private static final String runtimeReportWithIdUrlAndInc = runtimeReportWithIdUrl + "?inc";
    private final org.springframework.web.client.RestTemplate restTemplate;

    public RuntimeReportsClient(String serverUri, int serverPort, RestTemplate restTemplate) {
        Validate.notNull(serverUri);
        Validate.isTrue(serverPort > 0);

        this.serverUri = UriBuilder.fromUri(serverUri).port(serverPort).build();
        this.restTemplate = restTemplate;
        setupProxyToRestTemplate(this.serverUri, restTemplate);
    }

    private static void setupProxyToRestTemplate(URI serverUri, RestTemplate restTemplate) {
        Validate.notNull(serverUri);
        Validate.notNull(restTemplate);

        final ProxySelector proxySelector = ProxySelector.getDefault();
        Validate.notNull(proxySelector);

        final List<Proxy> proxies = proxySelector.select(serverUri);
        Validate.isTrue(proxies.size() > 0);

        final Proxy proxy = proxies.get(0);

        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setProxy(proxy);
        logger.info(String.format("Setting proxy: %s", proxy));
    }

    public void registerService(int id) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Registering runtime report for virtual service [Id=%d].", id));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final JSONObject json = new JSONObject().put(VsId, id).put(Count, 0);
            final ResponseEntity<String> response = restTemplate.postForEntity(serverUri + runtimeReportUrl, new HttpEntity<String>(json.toString(), headers), String.class);
            Validate.isTrue(response.getStatusCode() == HttpStatus.OK);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("POST returned response [%s].", response));
            }

        } catch (JSONException e) {
            logger.error(e);
            throw new RuntimeException();
        }
    }

    public void increaseServiceUsageCount(int id) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Incrementing counter for virtual service [Id=%d].", id));
        }

        final ResponseEntity<String> response = restTemplate.postForEntity(serverUri + runtimeReportWithIdUrlAndInc, null, String.class, id);
        Validate.isTrue(response.getStatusCode() == HttpStatus.OK);
    }

    public int getServiceUsageCount(int id) {
        final ResponseEntity<String> response = restTemplate.getForEntity(serverUri + runtimeReportWithIdUrl, String.class, id);
        Validate.isTrue(response.getStatusCode() == HttpStatus.OK);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Returned response for virtual service [Id=%d]: [%s].", id, response.toString()));
        }

        try {
            final JSONObject json = new JSONObject(response.getBody());
            final int count = json.getInt(Count);
            return count;
        } catch (JSONException e) {
            logger.error(e);
            throw new RuntimeException();
        }
    }

    public void unregisterService(int id) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Unregistering virtual service [Id=%d].", id));
        }

        restTemplate.delete(serverUri + runtimeReportWithIdUrl, id);
    }
}

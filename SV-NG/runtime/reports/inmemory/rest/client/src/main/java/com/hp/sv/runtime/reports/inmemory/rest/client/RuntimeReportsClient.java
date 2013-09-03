package com.hp.sv.runtime.reports.inmemory.rest.client;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class RuntimeReportsClient implements com.hp.sv.runtime.reports.api.RuntimeReportsClient {
    private static final Log logger = LogFactory.getLog(RuntimeReportsClient.class);

    private static final String serverUrl = "http://localhost:9998";
    private static final String VsId = "vsId";
    private static final String Count = "count";

    private static final String runtimeReportUrl = serverUrl + "/runtime-report";
    private static final String runtimeReportWithIdUrl = runtimeReportUrl + "/{id}";
    private static final String runtimeReportWithIdUrlAndInc = runtimeReportWithIdUrl + "?inc";

    private org.springframework.web.client.RestTemplate restTemplate;

    public RuntimeReportsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void registerService(int id) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Registering runtime report for virtual service [Id=%d].", id));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final JSONObject json = new JSONObject().put(VsId, id).put(Count, 0);
            final ResponseEntity<String> response = restTemplate.postForEntity(runtimeReportUrl, new HttpEntity<String>(json.toString(), headers), String.class);
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

        final ResponseEntity<String> response = restTemplate.postForEntity(runtimeReportWithIdUrlAndInc, null, String.class, id);
        Validate.isTrue(response.getStatusCode() == HttpStatus.OK);
    }
    public int getServiceUsageCount(int id) {
        final ResponseEntity<String> response = restTemplate.getForEntity(runtimeReportWithIdUrl, String.class, id);
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
    }
}

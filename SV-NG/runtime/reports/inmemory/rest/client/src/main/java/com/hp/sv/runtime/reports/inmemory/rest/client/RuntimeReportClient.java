package com.hp.sv.runtime.reports.inmemory.rest.client;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class RuntimeReportClient implements RuntimeReportsClient {
    private static final Log logger = LogFactory.getLog(RuntimeReportClient.class);

    private static final String serverUrl = "http://localhost:8085";
    private static final String VsId = "vsId";
    private static final String Count = "count";

    private org.springframework.web.client.RestTemplate restTemplate;

    public RuntimeReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void registerService(int id) {
        if(logger.isDebugEnabled()) {
            logger.debug(String.format("Registering runtime report for virtual service [Id=%d].", id));
        }

        try {
            final JSONObject json = new JSONObject().put(VsId, id).put(Count, 0);
            final URI uri = restTemplate.postForLocation(serverUrl + "/runtime-report/{id}", json, id);

            if(logger.isDebugEnabled()) {
                logger.debug(String.format("POST returned url [%s].", uri));
            }

        } catch (JSONException e) {
            logger.error(e);
            throw new RuntimeException();
        }
    }

    public void increaseServiceUsageCount(int id) {
    }

    public int getServiceUsageCount(int id) {
        final ResponseEntity<String> entity = restTemplate.getForEntity(serverUrl + "/runtime-report/{id}", String.class, id);

        if(logger.isDebugEnabled()) {
            logger.debug(String.format("Returned response entity for virtual service [Id=%d]: [%d].", id, entity));
        }

        try {
            final JSONObject json = new JSONObject(entity.getBody());
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

package com.hp.sv.runtime.reports.inmemory.rest.client;

import com.hp.sv.runtime.reports.api.RuntimeReportsClientException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

public class RuntimeReportsClientResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        }catch(Exception e) {
            throw new RuntimeReportsClientException("Runtime report client request execution failed.", e);
        }
    }
}

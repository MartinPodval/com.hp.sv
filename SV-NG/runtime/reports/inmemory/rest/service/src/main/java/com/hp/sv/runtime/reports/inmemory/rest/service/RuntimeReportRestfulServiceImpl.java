package com.hp.sv.runtime.reports.inmemory.rest.service;

import com.hp.sv.runtime.reports.api.RuntimeReportsService;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("runtime-report")
public class RuntimeReportRestfulServiceImpl {
    private static final Log logger = LogFactory.getLog(RuntimeReportRestfulServiceImpl.class);
    public static final String VsId = "vsId";
    public static final String Count = "count";

    private RuntimeReportsService service;

    public RuntimeReportRestfulServiceImpl(RuntimeReportsService service) {
        this.service = service;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void post(final JSONObject json) throws JSONException {
        Validate.notNull(json);

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Incoming json object: %s", json));
        }

        int vsId = json.getInt(VsId);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Posting runtime report for virtual service [Id=%d]", vsId));
        }

        service.registerService(vsId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@QueryParam("virtualService") final int virtualServiceId) throws JSONException {
        Validate.isTrue(virtualServiceId > 0);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Getting runtime report for virtual service [Id=%d]", virtualServiceId));
        }

        int count = service.getServiceUsageCount(virtualServiceId);
        final JSONObject json = new JSONObject().put(VsId, virtualServiceId).put(Count, count);

        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }
}

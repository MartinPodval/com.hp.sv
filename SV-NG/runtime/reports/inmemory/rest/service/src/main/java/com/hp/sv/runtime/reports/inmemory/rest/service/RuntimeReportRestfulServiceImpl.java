package com.hp.sv.runtime.reports.inmemory.rest.service;

import com.hp.sv.runtime.reports.api.RuntimeReportsService;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/runtime-report")
public class RuntimeReportRestfulServiceImpl {
    private static final Log logger = LogFactory.getLog(RuntimeReportRestfulServiceImpl.class);
    private static final String VsId = "vsId";
    private static final String Count = "count";

    @Autowired
    private RuntimeReportsService service;

    public RuntimeReportRestfulServiceImpl() {
    }

    public RuntimeReportRestfulServiceImpl(RuntimeReportsService service) {
        this.service = service;
    }

    @POST
    @Path("/")
    @Produces
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(final String json) throws JSONException {
        Validate.notNull(json);

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Incoming json object: %s", json));
        }

        int vsId = new JSONObject(json).getInt(VsId);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Posting runtime report for virtual service [Id=%d]", vsId));
        }

        service.registerService(vsId);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") final int id) throws JSONException {
        Validate.isTrue(id > 0);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Getting runtime report for virtual service [Id=%d]", id));
        }

        int count = service.getServiceUsageCount(id);
        final JSONObject json = new JSONObject().put(VsId, id).put(Count, count);

        return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
    }
}

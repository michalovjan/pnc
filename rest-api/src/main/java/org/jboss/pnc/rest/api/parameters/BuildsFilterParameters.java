package org.jboss.pnc.rest.api.parameters;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BuildsFilterParameters {

    @Parameter(description = "Should return only latest build?")
    @QueryParam("latest")
    @DefaultValue("false")
    boolean latest;

    @Parameter(description = "Should return only running builds?")
    @QueryParam("running")
    @DefaultValue("false")
    boolean running;
}

package br.com.brunoeas;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class HealthCheckResource {

    @GET
    public Response ok() {
        return Response.ok().build();
    }

}

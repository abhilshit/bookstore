package net.tg.webinar.demo.bookstore.filters;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import net.tg.webinar.demo.bookstore.annotations.SecuredWithCustom2;
import net.tg.webinar.demo.bookstore.service.AuthService;

@SecuredWithCustom2
@Provider
@Priority(Priorities.AUTHENTICATION)
public class CustomAuthFilter2 implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String accessTokenHeader = requestContext.getHeaderString("-x-access-token");

		boolean isUserValid = false;
		if (accessTokenHeader != null) {

			if (AuthService.accessTokens.getIfPresent(accessTokenHeader) != null) {
				isUserValid = true;
			}
		}

		if (!isUserValid) {
			ResponseBuilder builder = null;
			String result = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
			builder = Response.status(Response.Status.UNAUTHORIZED).entity(result);
			throw new WebApplicationException(builder.build());
		}
	}

	
}

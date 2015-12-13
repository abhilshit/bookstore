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

import net.tg.webinar.demo.bookstore.annotations.SecuredWithBASIC;

@SecuredWithBASIC
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		boolean isUserValid=false;
       if(authorizationHeader !=null){
        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.replace("Basic","").trim();
        isUserValid = validateToken(token);
       }
       
       if(!isUserValid){
    	   ResponseBuilder builder = null;
    	   String result = "{"+"\"status\":"+"\"Error\", \"message\":"+"\"User Not Authenticated\"} ";
           builder = Response.status(Response.Status.UNAUTHORIZED).entity(result);
           throw new WebApplicationException(builder.build());
       }
	}

	private boolean validateToken(String token) {
		
		String decodedToken = new String(Base64.decode(token));
		final String[] values = decodedToken.split(":",2);
		System.out.println("UserName: "+ values[0]);
		System.out.println("Password: "+ values[1]);
		//TODO: check with database whether the credentials are correct
		return true;
	}
	

}

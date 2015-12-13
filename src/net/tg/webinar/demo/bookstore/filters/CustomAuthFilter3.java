package net.tg.webinar.demo.bookstore.filters;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import net.tg.webinar.demo.bookstore.annotations.SecuredWithCustom3;
import net.tg.webinar.demo.bookstore.service.AuthService;
import net.tg.webinar.demo.bookstore.util.HashingUtil;

@SecuredWithCustom3
@Provider
@Priority(Priorities.AUTHENTICATION)
public class CustomAuthFilter3 implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String accessTokenHeader = requestContext.getHeaderString("-x-access-token");
		String requestSignature = requestContext.getHeaderString("-x-signature");
		String nonce = requestContext.getHeaderString("-x-nonce");
		String timestamp = requestContext.getHeaderString("-x-timestamp");
		
		boolean isUserValid = false;
		if (accessTokenHeader != null) {
			String user = AuthService.accessTokens.getIfPresent(accessTokenHeader);
			if (user != null) {
				try{

					String apiKey = AuthService.API_KEY_REGISTRY.get(user);
					String signature = HashingUtil.getRequestSignature(requestContext, apiKey);
					ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
					long currentTime = now.toInstant().toEpochMilli();
					long fiveMins = 1000*60*5;
					long threshold  = new Long(timestamp).longValue()+fiveMins;
					
					if(requestSignature.equals(signature) && (currentTime< threshold)){
						//the request is good to be processed, no one modified in between
						//Check for replay attacks
						List<String> nonces = AuthService.NONCE_REGISTRY.get(accessTokenHeader);
						if(nonces==null || !nonces.contains(nonce))
						{
							isUserValid = true;
							if(nonces==null ){
								nonces= new ArrayList<>();
							}
							
							nonces.add(nonce);
							AuthService.NONCE_REGISTRY.put(accessTokenHeader, nonces);
						}
					}
					else{
						//request is not valid
						isUserValid=false;
					}
				}
				catch(Exception e){
					//signatures not matching
					//TODO: Handle what to do
					throwAuthError();
				}
			}
		}

		if (!isUserValid) {
			throwAuthError();
		}
	}

	private void throwAuthError() {
		ResponseBuilder builder = null;
		String result = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
		builder = Response.status(Response.Status.UNAUTHORIZED).entity(result);
		throw new WebApplicationException(builder.build());
	}

	
	
}

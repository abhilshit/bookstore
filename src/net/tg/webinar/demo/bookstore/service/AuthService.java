package net.tg.webinar.demo.bookstore.service;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Path("/api/auth")
public class AuthService {

	// expiring map with time threshold when access tokens will expire
	public static Cache<String, String> accessTokens = CacheBuilder.newBuilder().removalListener(new RemovalListener<String, String>() {

		@Override
		public void onRemoval(RemovalNotification<String, String> removalNotification) {
			// remove all nonces once access token is expired
			String expiredToken = removalNotification.getKey();
			NONCE_REGISTRY.remove(expiredToken);
			
		}
		
	}).expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	/**
	 * Custom authentication version 1 assuming user passes the BASIC
	 * Authentication "Authorization: BASIC <token>" header TODO://This should
	 * be actually stored in database or a cache store
	 */

	@POST
	@Path("/v1")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticate(@HeaderParam(HttpHeaders.AUTHORIZATION) String basicToken) {

		String[] credentials = extractCredentials(basicToken);
		String username = credentials[0];
		String password = credentials[1];

		if (validateToken(username, password)) {
			// generate access token
			String accessToken = UUID.randomUUID().toString();
			accessTokens.put(accessToken, username);
			String result = "{" + "\"status\":" + "\"success\"} ";
			return Response.status(200).entity(result).header("x-access-token", accessToken).build();
		} else {
			String result = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
			return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();

		}

	}

	/**
	 * Custom authentication version 2 assuming user has registered for
	 * consuming web service by generating API_KEY TODO://This should be
	 * actually stored in database or a cache store
	 */

	public static HashMap<String, String> API_KEY_REGISTRY = new HashMap<>();

	static {

		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16]; // 128 bits are converted to 16 bytes;

		random.nextBytes(bytes);
		String user1APIKey =  new BASE64Encoder().encode(bytes);
		
		//API_KEY_REGISTRY.put("user1", user1APIKey);
		
		// HARD Coded just for demo
		API_KEY_REGISTRY.put("user1", "egEmfIwA6dyFmHoKjjjsQw==");
		
		System.out.println("API Key for user1: "+  user1APIKey);

		random.nextBytes(bytes);
		String user2APIKey = new BASE64Encoder().encode(bytes);
		API_KEY_REGISTRY.put("user2", user2APIKey);
		System.out.println("API Key for user2: "+user2APIKey);

	}

	@POST
	@Path("/v2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticate2(@HeaderParam("-x-api-key") String apiKey, @HeaderParam("-x-api-user") String user) {

		if (API_KEY_REGISTRY.get(user).equals(apiKey)) {
			// generate access token
			String accessToken = UUID.randomUUID().toString();
			accessTokens.put(accessToken, user);
			String result = "{" + "\"status\":" + "\"success\"} ";
			return Response.status(200).entity(result).header("x-access-token", accessToken).build();
		} else {
			String result = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
			return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();

		}

	}

	
	/**
	 * Custom authentication version 3
	 * 
	 */	
	// expiring map with time threshold when challenge answers will expire
	//It is better to expect challenge answer within a few seconds so that no one can send fake answer requests
		public static Cache<String, HashMap<String, String>> CHALLENGE_ANSWER_REGISTRY = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
				.build();
	
		public static HashMap<String, List<String>> NONCE_REGISTRY = new HashMap<>();
		
	@POST
	@Path("/v3")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticate3(@HeaderParam("-x-api-user") String user) {
		String errorResult = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
		
		String apiKey = API_KEY_REGISTRY.get(user);
		if (apiKey != null) {
			try {
				String answer = UUID.randomUUID().toString();

				Cipher c = Cipher.getInstance("AES");
				SecretKeySpec k = new SecretKeySpec(Base64.decode(apiKey), "AES");

				c.init(Cipher.ENCRYPT_MODE, k);
				byte[] encryptedData = c.doFinal(answer.getBytes());

				String challenge = new String(Base64.encode(encryptedData));
				HashMap<String,String>  userChallengeMap = new HashMap<String, String>();
				userChallengeMap.put(challenge, answer);
				CHALLENGE_ANSWER_REGISTRY.put(user,userChallengeMap);
				
//				// generate access token
//				String accessToken = UUID.randomUUID().toString();
//				accessTokens.put(accessToken, user);
				String result = "{" + "\"status\":" + "\"success\"} ";
				return Response.status(200).entity(result).header("-x-challenge", challenge).build();
			} catch (Exception e) {
				return Response.status(Response.Status.UNAUTHORIZED).entity(errorResult).build();
			}
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorResult).build();

		}

	}
	@POST
	@Path("/v3/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getToken(@HeaderParam("-x-api-user") String user, @HeaderParam("-x-challenge-answer") String answer ) {
		String errorResult = "{" + "\"status\":" + "\"Error\", \"message\":" + "\"User Not Authenticated\"} ";
		
		HashMap<String,String>  userChallengeMap = CHALLENGE_ANSWER_REGISTRY.getIfPresent(user);
		boolean userAuthenticated =false;
		for(Entry<String,String> challengeEntry:userChallengeMap.entrySet()){
			
			if(challengeEntry.getValue().equals(answer)){
				userAuthenticated=true;
			}
			
		}
		if (userAuthenticated) {
			try {
				// generate access token
				String accessToken = UUID.randomUUID().toString();
				accessTokens.put(accessToken, user);
				String result = "{" + "\"status\":" + "\"success\"} ";
				
				return Response.status(200).entity(result).header("-x-access-token", accessToken).build();
			} catch (Exception e) {
				return Response.status(Response.Status.UNAUTHORIZED).entity(errorResult).build();
			}
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorResult).build();

		}

	}

	private String[] extractCredentials(String token) {
		token = token.replace("Basic","").trim();
		String decodedToken = new String(Base64.decode(token));
		final String[] values = decodedToken.split(":", 2);
		System.out.println("UserName: " + values[0]);
		System.out.println("Password: " + values[1]);
		return values;
	}

	private boolean validateToken(String username, String password) {

		// TODO: check with database whether the credentials are correct
		return true;
	}

}

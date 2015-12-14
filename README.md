# Bookstore 

The bookstore app contains samples demonstrating how web services can be secured using 

	1. HTTP BASIC Authentication
	2. Custom Token Based Authentication v 1.0
	3. Custom Token Based Authentication v 2.0
	4. Custom Token Based Authentication v 3.0

as discussed in the Techgig webinar refer the following webpage for the same.
	
		http://www.techgig.com/webinar/Building-Secure-RESTful-Web-Services-835

The code uses Jax-RS RI annotations along with Apache Jersey. The code should work with any other JAX-RS compliant libraries like RestEasy or Apache CXF with minor changes.

#Implementation Approach

I have used JAX-RS @NameBinding to create custom annotation with which resources can be bounded with ContainerRequestFilter

For eg.

The following code snippet creates a NameBinding for @SecuredWithCustom3 annotation

	@NameBinding
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	public @interface SecuredWithCustom3 { 
		
	}  

The @SecuredWithCustom3 can be added on top of a Filter class and a web resource so that the filter executes whenever the resource is accessed

Refer CustomAuthFilter3.java and getBooksById3() method of BookService.java
	
	@SecuredWithCustom3
	@Provider
	@Priority(Priorities.AUTHENTICATION)
	public class CustomAuthFilter3 implements ContainerRequestFilter {
	
	}



	@Path("/api")
	public class BookService {
	...
		@GET
	    @Path("v3/books/{id}")
		@SecuredWithCustom3
	    @Produces(MediaType.APPLICATION_JSON)
	    public Book getBooksById3(@PathParam("id") Long id) {
	        Book bookById = null;
			for(Book book:books)
	        {
	        	if(book.getId()==id)
	        	{
	        		bookById=book;
	        	}
	        }
	        return bookById;
	    }
	
	}

As you can see we have added this annotation @SecuredWithCustom3 on top of CustomAuthFilter3 which means that this filter is executed when a resource v3/books/{id} (see getBooksById3 method annotated with @SecuredWithCustom3) is accessed.


For the following authentication mechanisms

	1. Custom Token Based Authentication v 1.0
	2. Custom Token Based Authentication v 2.0
	3. Custom Token Based Authentication v 3.0

A separate /auth endpoint is required to obtain access token initially.  This endpoint is present inside 

	@Path("/api/auth")
	public class AuthService {
	
	}
	
For eg. See the following /auth and /token endpoints for obtaining access tokens as part of Custom Token Based Authentication v 3.0

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

#Implementing Mutual SSL Authentication

The code does not contain a sample for implementing Mutual SSL Authentication as it requires changes at the application server level and not at the code level.
It can be implemented in following way

1. Issue client certificates to your users and make them add it to their key stores. 
2. Generate Client Certificates and add the Client’s Public Certificate to the Server’s Trust store
	
		keytool -genkeypair -alias serverkey -keyalg RSA -dname "CN=Web Server,OU=Development,O=ABC Corp,L=XYZ S=FL,C=US" 	-keypass password -keystore 	server.jks -storepass password
	 
	 	keytool -genkeypair -alias clientkey -keyalg RSA -dname "CN=client,OU=Development,O=ABC Corp,L=XYZ,S=FL,C=US" 	-keypass password -storepass password 	-keystore client.jks
	
	 	keytool -exportcert -alias clientkey -file client-public.cer -keystore client.jks -storepass password keytool 	-importcert -keystore server.jks -alias clientcert -file client-public.cer -storepass password -noprompt 
	 
	# view the contents of the keystore (use -v for verbose output) 
	keytool -list -keystore server.jks -storepass password
3. Ask clients to store server’s public certificate to their trust store.
4. Export the Server’s Public Certificate and Import it in to the Client’s Keystore

		keytool -exportcert -alias serverkey -file server-public.cer -keystore server.jks -storepass password 
	keytool -importcert -keystore client.jks -alias servercert -file server-public.cer -storepass password -noprompt 
	
	# view the contents of the keystore (use -v for verbose output) 
	keytool -list -keystore client.jks -storepass password
5. Configure the server to ask for client certificates while requesting a resource URI
6. Typical tomcat server.xml HTTP connector configuuration

	<Connector clientAuth="true" port="8443" minSpareThreads="5" maxSpareThreads="75" enableLookups="true" 	disableUploadTimeout="true" acceptCount="100" maxThreads="200" scheme="https" secure="true" SSLEnabled="true" 	keystoreFile="/Users/mporges/Desktop/tomcat-ssl/final/server.jks" keystoreType="JKS" keystorePass="password" 	truststoreFile="/Users/mporges/Desktop/tomcat-ssl/final/server.jks" truststoreType="JKS" truststorePass="password" 	SSLVerifyClient="require" SSLEngine="on" SSLVerifyDepth="2" sslProtocol="TLS" />

#Implementing OAuth 1a and OAuth 2

The code does not contain a sample for implementing OAuth 1a or OAuth 2 as there are already libraries available that can be used to create an OAuth Provider or OAuth Consumer

I would suggest to look into Spring OAuth Sparklr and Tonr sample apps 
	
	http://projects.spring.io/spring-security-oauth/docs/tutorial.html

You can also refer to Apache Oltu https://oltu.apache.org/ which is also a Java Library that provides support for OAuth Implementation
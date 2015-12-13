package net.tg.webinar.demo.bookstore.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.message.internal.ReaderWriter;

import sun.misc.BASE64Encoder;

public class HashingUtil {
	public static String hmacSha1Encode(String key, String message) throws Exception{

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(keySpec);
		byte[] rawHmac = mac.doFinal(message.getBytes());

		BASE64Encoder encoder = new BASE64Encoder();
		String encodedHMac = encoder.encode(rawHmac);
		System.out.println("Signature Generated:" + encodedHMac);

		return encodedHMac;
	}

	public static String getEntityBody(ContainerRequestContext requestContext) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = requestContext.getEntityStream();

		final StringBuilder b = new StringBuilder();
		try {
			ReaderWriter.writeTo(in, out);

			byte[] requestEntity = out.toByteArray();
			if (requestEntity.length == 0) {
				b.append("").append("\n");
			} else {
				b.append(new String(requestEntity)).append("\n");
			}
			requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));

		} catch (IOException ex) {
			// Handle logging error
		}
		return b.toString();
	}

	public static String generateSignature(String apikey, String url, String messageBody, String method,
			MultivaluedMap<String, String> headers) throws Exception {

		String signature = null;
		
		StringBuffer stringToSign = new StringBuffer();
		stringToSign.append(method);
		stringToSign.append(url);
		stringToSign.append(messageBody);
		
		SortedSet<String> headerKeys = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		
		headerKeys.addAll(headers.keySet());
		
		for(String headerKey: headerKeys){
			if(!headerKey.contains("-x-signature")&&headerKey.startsWith("-x")){
				stringToSign.append(headerKey+""+headers.getFirst(headerKey));
			}
		}
		
		return hmacSha1Encode(apikey,stringToSign.toString());
	}

	public static String getRequestSignature(ContainerRequestContext requestContext, String apiKey) throws Exception {

		String messageBody = HashingUtil.getEntityBody(requestContext);
		messageBody= messageBody.trim();
		String url = requestContext.getUriInfo().getAbsolutePath().toString();
		String method = requestContext.getMethod();
		MultivaluedMap<String, String> headers = requestContext.getHeaders();
		return generateSignature(apiKey, url, messageBody,method, headers);
	}
}

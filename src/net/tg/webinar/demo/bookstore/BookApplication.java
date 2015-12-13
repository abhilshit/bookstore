package net.tg.webinar.demo.bookstore;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;


@ApplicationPath("/")
public class BookApplication extends ResourceConfig {
 
	public BookApplication() {
         packages("net.tg.webinar.demo.bookstore");
         register(JacksonFeature.class);
    }
   
}
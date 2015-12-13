package net.tg.webinar.demo.bookstore.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.tg.webinar.demo.bookstore.annotations.SecuredWithBASIC;
import net.tg.webinar.demo.bookstore.annotations.SecuredWithCustom1;
import net.tg.webinar.demo.bookstore.annotations.SecuredWithCustom2;
import net.tg.webinar.demo.bookstore.annotations.SecuredWithCustom3;
import net.tg.webinar.demo.bookstore.model.Book;

@Path("/api")
public class BookService {
	
	public static List<Book> books = new ArrayList<>();
	
	static
	{
		Book b1 = new Book(1, "Alchemist", "Paulo Cohelo");
		Book b2 = new Book(2, "My experiments with Truth", "Mahatma M.K.Gandhi");
		Book b3 = new Book(3, "Far from the Madding Crowd", "Thomas Hardy");
		Book b4 = new Book(4, "The Merchant of venice", "William shakespeare");
		Book b5 = new Book(5, "Mein Kampf", "Adolf Hitler");
		books.add(b1);
		books.add(b2);
		books.add(b3);
		books.add(b4);
		books.add(b5);
	}
	
	
	@GET
    @Path("/books/{id}")
	@SecuredWithBASIC
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBooksById(@PathParam("id") Long id) {
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

	@GET
    @Path("v1/books/{id}")
	@SecuredWithCustom1
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBooksById1(@PathParam("id") Long id) {
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
	
	@GET
    @Path("v2/books/{id}")
	@SecuredWithCustom2
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBooksById2(@PathParam("id") Long id) {
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
	
	
    @POST
    @SecuredWithBASIC
    @Path("/books")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBook(Book book) {
    	String result = "{"+"\"status\":"+"\"success\"} ";
    	books.add(book);
		return Response.status(201).entity(result).build();
    }

}

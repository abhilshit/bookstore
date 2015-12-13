package net.tg.webinar.demo.bookstore.model;

public class Book {
	private long id;
	private String title;
	private String author;
	
	
	public Book(long id, String title, String author) {
		super();
		this.id = id;
		this.title = title;
		this.author = author;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "id="+this.id+ ", \n"+
				"title="+this.title+ ", \n"+
				"author="+this.author+ ", \n";
	}
	
	

}

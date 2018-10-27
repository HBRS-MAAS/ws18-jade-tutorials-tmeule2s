package maas;

import java.io.Serializable;

public class Book implements Serializable {
	private static final long serialVersionUID = 1411574590379986584L;

	private BookType bookType;

	private String title;

	private Double price;

	public Book() {
		// TODO Auto-generated constructor stub
	}

	public Book(Book toClone) {
		this.bookType = toClone.bookType;
		this.title = toClone.title;
		this.price = toClone.price;
	}

	public BookType getBookType() {
		return bookType;
	}

	public void setBookType(BookType bookType) {
		this.bookType = bookType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}

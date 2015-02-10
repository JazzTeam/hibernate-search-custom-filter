package org.jazzteam.service;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.jazzteam.model.Book;
import org.jazzteam.utils.HibernateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BookSearchServiceTest {

    private BookSearchService bookSearchService;

    @Before
    public void setUp() throws Exception {
        FullTextSession session = HibernateUtil.getSession();
        this.bookSearchService = new BookSearchService(session);
        List<Book> books = new ArrayList<Book>();

        books.add(book("Refactoring", "M.Fowler", 5));
        books.add(book("Crockford's JavaScript", "Douglas Crockford", 3.2));
        books.add(book("Developing Backbone.js Applications", "Addy Osmani", 3.1));
        books.add(book("Agile Java", "Daniel Selman", 4.1));
        books.add(book("UNIX Toolbox", "Colin Barschel", 4.6));
        books.add(book("Commentary on the Sixth Edition UNIX", "J. Lions", 4.9));

        saveBooks(session, books);
    }

    private Book book(String name, String author, double rating) {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setRating(rating);
        return book;
    }

    private void saveBooks(FullTextSession session, List<Book> books) {
        Transaction tx = session.beginTransaction();
        try {
            for (Book book : books) {
                session.save(book);
            }
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    public void testFindBooksWithSpecificRating() throws Exception {
        List<Book> unixBooks = bookSearchService.findBooksWithSpecificRating("UNIX", 4.7, 5);

        assertEquals(1, unixBooks.size());
        assertEquals("Commentary on the Sixth Edition UNIX", unixBooks.get(0).getName());
        assertEquals("J. Lions", unixBooks.get(0).getAuthor());

        List<Book> emptyResult = bookSearchService.findBooksWithSpecificRating("M.Fowler", 1.1, 3);

        assertTrue(emptyResult.isEmpty());

    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.closeSession();
    }
}

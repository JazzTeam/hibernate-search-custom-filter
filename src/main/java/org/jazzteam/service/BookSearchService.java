package org.jazzteam.service;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jazzteam.filter.BookRatingFilter;
import org.jazzteam.model.Book;

import java.util.List;

public class BookSearchService {
    private static final String RATING_FIELD_NAME = "rating";
    private static final Class<Book> TARGET_CLASS = Book.class;
    private final FullTextSession session;

    public BookSearchService(FullTextSession session) {
        this.session = session;
    }

    public List<Book> findBooksWithSpecificRating(String bookOrAuthorName, double lowerRatingBound,
            double upperRatingBound)
            throws InterruptedException {
        QueryBuilder qb = session
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(TARGET_CLASS)
                .get();

        Query luceneQuery = qb
                .keyword()
                .fuzzy()
                .onFields(new String[] {RATING_FIELD_NAME, "name", "author"})
                .matching(bookOrAuthorName)
                .createQuery();

        luceneQuery = applyRatingFilter(luceneQuery, RATING_FIELD_NAME, lowerRatingBound,
                upperRatingBound);

        FullTextQuery fullTextQuery =
                session.createFullTextQuery(luceneQuery, new Class[] {TARGET_CLASS});

        return fullTextQuery.list();
    }

    private Query applyRatingFilter(Query previousQuery, String fieldName, double lowerRatingBound,
            double upperRatingBound) {
        if (lowerRatingBound >= 0) {
            QueryWrapperFilter wrapper = new QueryWrapperFilter(previousQuery);
            Filter ratingFilter =
                    new BookRatingFilter(wrapper, fieldName, lowerRatingBound, upperRatingBound);
            return new ConstantScoreQuery(ratingFilter);
        } else {
            throw new IllegalArgumentException("Invalid value of rating.");
        }
    }
}

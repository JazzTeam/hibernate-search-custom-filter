package org.jazzteam.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import java.io.IOException;

public class BookRatingFilter extends Filter {
    private final double lowerRatingBound;
    private final QueryWrapperFilter previousFilter;
    private final double upperRatingBound;
    private String fieldName;

    public BookRatingFilter(QueryWrapperFilter previousFilter, String fieldName,
            double lowerRatingBound, double upperRatingBound) {
        this.fieldName = fieldName;
        this.lowerRatingBound = lowerRatingBound;
        this.upperRatingBound = upperRatingBound;
        this.previousFilter = previousFilter;
    }

    @Override
    public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException {
        final String[] allIndexedStrings = FieldCache.DEFAULT.getStrings(indexReader, fieldName);
        DocIdSet docs = previousFilter.getDocIdSet(indexReader);

        if ((docs == null) || (docs.iterator() == null)) {
            throw new IllegalStateException("DocIdSet cannot be null!");
        }
        return new FilteredDocIdSet(docs) {
            @Override
            protected boolean match(int documentIndex) {
                double rating = Double.parseDouble(allIndexedStrings[documentIndex]);
                return rating >= lowerRatingBound && rating <= upperRatingBound;
            }
        };
    }
}

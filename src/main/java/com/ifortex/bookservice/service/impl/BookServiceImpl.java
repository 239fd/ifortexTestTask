package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Long> getBooks() {

        List<Book> books = entityManager.createQuery("select b from Book b", Book.class).getResultList();

        Map<String, Long> genreCountMap = new LinkedHashMap<>();

        for (Book book : books) {
            for (String genre : book.getGenres()) {
                genreCountMap.put(genre, genreCountMap.getOrDefault(genre, 0L) +1);
            }
        }

        return genreCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

    }

    @Override
    public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {


        if (searchCriteria.getTitle() != null && !searchCriteria.getTitle().isEmpty()) {
            List<Book> books = entityManager.createQuery("select b from Book b", Book.class).getResultList();
        }
        if (searchCriteria.getAuthor() != null && !searchCriteria.getAuthor().isEmpty()) {
            predicate = cb.and(predicate, cb.like(cb.lower(book.get("author")), "%" + searchCriteria.getAuthor().toLowerCase() + "%"));
        }
        if (searchCriteria.getGenre() != null && !searchCriteria.getGenre().isEmpty()) {
            predicate = cb.and(predicate, cb.equal(cb.lower(book.get("genre")), searchCriteria.getGenre().toLowerCase()));
        }
        if (searchCriteria.getYear() != null) {
            predicate = cb.and(predicate, cb.equal(book.get("publicationYear"), searchCriteria.getYear()));
        }

        query.select(book).where(predicate).orderBy(cb.desc(book.get("publicationDate")));

        return entityManager.createQuery(query).getResultList();
    }
}

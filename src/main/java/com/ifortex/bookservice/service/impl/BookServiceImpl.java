package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

        if(searchCriteria == null) {
            return entityManager.createQuery("select b from Book b order by b.publicationDate desc", Book.class).getResultList();
        }

        List<Book> books = entityManager.createQuery("select b from Book b", Book.class).getResultList();

         return books.stream()
              .filter(b -> searchCriteria.getTitle() == null || searchCriteria.getTitle().isEmpty() ||
                      b.getTitle().toLowerCase().contains(searchCriteria.getTitle().toLowerCase()))
              .filter(b -> searchCriteria.getAuthor() == null || searchCriteria.getAuthor().isEmpty() ||
                      b.getAuthor().toLowerCase().contains(searchCriteria.getAuthor().toLowerCase()))
              .filter(b -> searchCriteria.getGenre() == null || searchCriteria.getGenre().isEmpty() ||
                      b.getGenres().stream().anyMatch(genre -> genre.equalsIgnoreCase(searchCriteria.getGenre())))
              .filter(b -> searchCriteria.getDescription() == null || searchCriteria.getDescription().isEmpty() ||
                      b.getDescription().toLowerCase().contains(searchCriteria.getDescription().toLowerCase()))
              .filter(b -> searchCriteria.getYear() == null ||
                      b.getPublicationDate().getYear() == searchCriteria.getYear())
              .sorted((b1,b2) -> b2.getPublicationDate().compareTo(b1.getPublicationDate()))
              .collect(Collectors.toList());
    }
}

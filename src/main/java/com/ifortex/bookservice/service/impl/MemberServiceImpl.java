package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Member findMember() {
        List<Member> members = entityManager.createQuery("SELECT m FROM Member m JOIN FETCH m.borrowedBooks b", Member.class)
                .getResultList();

        return members.stream()
                .filter(this::hasRomanceBook)
                .min(Comparator.comparing(
                        this::getOldestRomanceBookDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).thenComparing(Member::getMembershipDate, Comparator.reverseOrder()))
                .orElse(null);
    }

    @Override
    public List<Member> findMembers() {
        return entityManager.createQuery(
                        "SELECT m FROM Member m " +
                                "LEFT JOIN m.borrowedBooks b " +
                                "WHERE m.membershipDate BETWEEN :startOfYear AND :endOfYear " +
                                "AND b IS NULL", Member.class)
                .setParameter("startOfYear", LocalDateTime.of(2023, 1, 1, 0, 0))
                .setParameter("endOfYear", LocalDateTime.of(2023, 12, 31, 23, 59))
                .getResultList();
    }

    private boolean hasRomanceBook(Member member) {
        return member.getBorrowedBooks().stream()
                .anyMatch(book -> book.getGenres().stream().anyMatch("Romance"::equalsIgnoreCase));
    }

    private LocalDateTime getOldestRomanceBookDate(Member member) {
        return member.getBorrowedBooks().stream()
                .filter(book -> book.getGenres().stream().anyMatch("Romance"::equalsIgnoreCase))
                .map(Book::getPublicationDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

}

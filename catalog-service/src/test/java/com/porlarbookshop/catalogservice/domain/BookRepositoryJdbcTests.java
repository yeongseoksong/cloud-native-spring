package com.porlarbookshop.catalogservice.domain;

import com.porlarbookshop.catalogservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
public class BookRepositoryJdbcTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Test
    void findBookByIsbnWhenExisting(){
        var bookIsbn="1234561237" ;
        var book= Book.of(bookIsbn,"title","author",12.90,null);
        jdbcAggregateTemplate.insert(book);
        Optional<Book> actualBook = bookRepository.findByIsbn(bookIsbn);
        assertThat(actualBook).isPresent();
        assertThat(actualBook.get().isbn()).isEqualTo(book.isbn());
    }

    @Test
    void whenCreateBookNotAuthenticatedThenNoAuditMetadata(){
        var bookIsbn="1234561237" ;
        var book= Book.of(bookIsbn,"title","author",12.90,null);
        Book save = bookRepository.save(book);

        assertThat(save.createdBy()).isNull();
        assertThat(save.lastModifiedBy()).isNull();

    }

    @Test
    @WithMockUser("john")
    void whenCreateBookAuthenticatedThenAuditMetadata(){
        var bookIsbn="1234561237" ;
        var book= Book.of(bookIsbn,"title","author",12.90,null);
        Book save = bookRepository.save(book);

        assertThat(save.createdBy()).isEqualTo("john");
        assertThat(save.lastModifiedBy()).isEqualTo("john");

    }
}

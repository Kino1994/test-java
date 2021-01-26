package es.urjc.code.daw.library.webtestclient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerUnitTest {
	
	private WebTestClient webTestClient;
	
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;
    
    private List<Book> books;
    
	private Book book;

    @BeforeEach
    public void beforeEach() {
        webTestClient = MockMvcWebTestClient
        	.bindTo(mockMvc)
        	.build();
        
        books = new ArrayList<>();
		
		books.add(new Book("Book 1", "Description 1"));
		books.get(books.size() - 1).setId(1L);
		
		books.add(new Book("Book 2", "Description 2"));
		books.get(books.size() - 1).setId(2L);
		
		books.add(new Book("Book 3", "Description 3"));
		books.get(books.size() - 1).setId(3L);
		
		book = new Book("New Book", "New");
		book.setId(4);
    }
    
	@AfterEach
	public void afterEach() {
		books.clear();
		books = null;
		book = null;
	}

    @Test
    public void findAllOk() {
    	
        when(bookService.findAll()).thenReturn(books);

        BodyContentSpec bodyContentSpec = webTestClient.get()
        	.uri("/api/books/")
        	.exchange()
	        .expectStatus()
	        .isOk()
	        .expectBody()	            
        	.jsonPath("$")
	        .value(hasSize(3));
        
        for (int i = 0; i < books.size(); i++) {
        	bodyContentSpec.jsonPath("$[" + i + "].id")
        		.value(equalTo(books.get(i).getId().intValue()));	
        	
        	bodyContentSpec.jsonPath("$[" + i + "].title")
    			.value(equalTo(books.get(i).getTitle()));	
        	
        	bodyContentSpec.jsonPath("$[" + i + "].description")
    			.value(equalTo(books.get(i).getDescription()));	
		}
    }

    @Test
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    public void createBookOk() throws JsonProcessingException {
    	
    	when(bookService.save(any(Book.class))).thenReturn(book);

        webTestClient.post()
            .uri("/api/books/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(objectMapper.writeValueAsString(book)))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("$.id").isEqualTo(book.getId())
            .jsonPath("$.title").isEqualTo(book.getTitle())
            .jsonPath("$.description").isEqualTo(book.getDescription());
    }

    @Test
    @WithMockUser(username = "admin", password = "pass", roles = "ADMIN")
    public void deleteBookOk() {
    	
    	doNothing().when(bookService).delete(3);

        webTestClient.delete()
            .uri("/api/books/3")
            .exchange()
            .expectStatus()
            .isOk();
    }
    
}
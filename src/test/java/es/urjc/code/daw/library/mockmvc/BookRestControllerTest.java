package es.urjc.code.daw.library.mockmvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private BookService bookService;

	@Autowired
	private ObjectMapper objectMapper;

	private List<Book> books;

	private Book book;

	@BeforeEach
	public void beforeEach() {
		
		books = new ArrayList<>();
		
		books.add(new Book("Book 1", "Description 1"));
		books.get(books.size() - 1).setId(1);
		
		books.add(new Book("Book 2", "Description 2"));
		books.get(books.size() - 1).setId(2);
		
		books.add(new Book("Book 3", "Description 3"));
		books.get(books.size() - 1).setId(3);
		
		book = new Book("New Book", "New");
		book.setId(6);
	}

	@AfterEach
	public void afterEach() {
		books.clear();
		books = null;
		book = null;
	}

	@Test
	public void getBooksOk() throws Exception {

		when(bookService.findAll()).thenReturn(books);

		ResultActions resultActions = mvc.perform(get("/api/books/").
			contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(3)));
		
		for (int i = 0; i < books.size(); i++) {
			resultActions.andExpect(jsonPath("$[" + i + "].id", equalTo(books.get(i).getId().intValue())))
			.andExpect(jsonPath("$[" + i + "].title", equalTo(books.get(i).getTitle())))
			.andExpect(jsonPath("$[" + i + "].description", equalTo(books.get(i).getDescription())));			
		}
			
	}

	@Test
	@WithMockUser(username = "user", password = "pass", roles = "USER")
	public void createBookOk() throws Exception {

		when(bookService.save(any(Book.class))).thenReturn(book);

		mvc.perform(post("/api/books/")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(book)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", equalTo(book.getId().intValue())))
			.andExpect(jsonPath("$.title", equalTo(book.getTitle())))
			.andExpect(jsonPath("$.description", equalTo(book.getDescription())));
	}

	@Test
	@WithMockUser(username = "admin", password = "pass", roles = "ADMIN")
	public void deleteBookOk() throws Exception {
		
		doNothing().when(bookService).delete(2);
		
		mvc.perform(delete("/api/books/2"))
			.andExpect(status().isOk());
	}

}

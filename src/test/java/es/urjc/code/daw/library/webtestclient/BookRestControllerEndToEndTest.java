package es.urjc.code.daw.library.webtestclient;

import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import javax.net.ssl.SSLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.urjc.code.daw.library.book.Book;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestControllerEndToEndTest {

	private WebTestClient webTestClient;

	@LocalServerPort
	private int port;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Book book;
	
	@BeforeEach
	public void beforeEach() throws SSLException {
		
		SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
		
		HttpClient httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext))
				.baseUrl("https://localhost:" + port);
		
		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		
		webTestClient = WebTestClient.bindToServer(connector).build();
		
		book = new Book("New Book", "New");
		book.setId(8);
	}

	@Test
	public void findAllOk() {
		webTestClient.get()
			.uri("/api/books/")	// Seems deleteBook test is always executed before. So there is one item less: 4 items.
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$")
            .value(hasSize(4))
            .jsonPath("[0].id").value(equalTo(2))
			.jsonPath("[0].title").value(equalTo("LA VIDA SECRETA DE LA MENTE"))
			.jsonPath("[0].description").value(equalTo("La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos."))	
			.jsonPath("[1].id").value(equalTo(3))
			.jsonPath("[1].title").value(equalTo("CASI SIN QUERER"))
			.jsonPath("[1].description").value(equalTo("El amor algunas veces es tan complicado como impredecible. Pero al final lo que más valoramos son los detalles más simples, los más bonitos, los que llegan sin avisar. Y a la hora de escribir sobre sentimientos, no hay nada más limpio que hacerlo desde el corazón. Y eso hace Defreds en este libro."))
			.jsonPath("[2].id").value(equalTo(4))
			.jsonPath("[2].title").value(equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR"))
			.jsonPath("[2].description").value(equalTo("Recopilación de nuevos poemas, textos en prosa y pensamientos del autor. Un sabio dijo una vez: «Pocas cosas hipnotizan tanto en este mundo como una llama y como la luna, será porque no podemos cogerlas o porque nos iluminan en la penumbra». Realmente no sé si alguien dijo esta cita o me la acabo de inventar pero deberían de haberla escrito porque el poder hipnótico que ejercen esa mujer de rojo y esa dama blanca sobre el ser humano es digna de estudio."))
			.jsonPath("[3].id").value(equalTo(5))
			.jsonPath("[3].title").value(equalTo("LA LEGIÓN PERDIDA"))
			.jsonPath("[3].description").value(equalTo("En el año 53 a. C. el cónsul Craso cruzó el Éufrates para conquistar Oriente, pero su ejército fue destrozado en Carrhae. Una legión entera cayó prisionera de los partos. Nadie sabe a ciencia cierta qué pasó con aquella legión perdida.150 años después, Trajano está a punto de volver a cruzar el Éufrates. ..."));
	}

	@Test
	public void createBookOk() throws JsonProcessingException {

		FluxExchangeResult<String> result = webTestClient.post()
			.uri("/api/books/")
			.headers(header -> header.setBasicAuth("user", "pass"))
	        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	        .body(BodyInserters.fromValue(objectMapper.writeValueAsString(book)))
	        .exchange()
	        .expectStatus()
	        .isCreated()
			.returnResult(String.class);

		int id = from(result.getResponseBody().blockFirst()).get("id");

		webTestClient.get()
			.uri("/api/books/" + id)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(id)
			.jsonPath("$.title").isEqualTo(book.getTitle())
			.jsonPath("$.description").isEqualTo(book.getDescription());
	}

	@Test
	public void deleteBookOk() {
		
		webTestClient.delete()
			.uri("/api/books/1")
            .headers(header -> header.setBasicAuth("admin", "pass"))
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.exchange()
			.expectStatus()
			.isOk();
		
		webTestClient.delete()
		.uri("/api/books/1")
        .headers(header -> header.setBasicAuth("admin", "pass"))
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus()
		.isNotFound();
		
	}
}
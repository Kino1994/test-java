package es.urjc.code.daw.library.restassured;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static io.restassured.path.json.JsonPath.from;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.urjc.code.daw.library.book.Book;
import io.restassured.RestAssured;
import io.restassured.response.Response;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestControllerTest {

	@LocalServerPort
	int port;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Book book;
	
	@BeforeEach
	public void beforeEach() {		
		RestAssured.port = port;
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.baseURI = "https://localhost:" + port;
		
		book = new Book("New Book", "New");
		book.setId(6);
	}

	@Test
	public void getBooksOk() {
		
		when()
			.get("/api/books/")	// Seems deleteBook test is always executed before. So there is one item less: 4 items.
		.then()
			.statusCode(200)
			.body("[0].id", equalTo(2))
			.body("[0].title", equalTo("LA VIDA SECRETA DE LA MENTE"))
			.body("[0].description", equalTo("La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos."))	
			.body("[1].id", equalTo(3))
			.body("[1].title", equalTo("CASI SIN QUERER"))
			.body("[1].description", equalTo("El amor algunas veces es tan complicado como impredecible. Pero al final lo que más valoramos son los detalles más simples, los más bonitos, los que llegan sin avisar. Y a la hora de escribir sobre sentimientos, no hay nada más limpio que hacerlo desde el corazón. Y eso hace Defreds en este libro."))
			.body("[2].id", equalTo(4))
			.body("[2].title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR"))
			.body("[2].description", equalTo("Recopilación de nuevos poemas, textos en prosa y pensamientos del autor. Un sabio dijo una vez: «Pocas cosas hipnotizan tanto en este mundo como una llama y como la luna, será porque no podemos cogerlas o porque nos iluminan en la penumbra». Realmente no sé si alguien dijo esta cita o me la acabo de inventar pero deberían de haberla escrito porque el poder hipnótico que ejercen esa mujer de rojo y esa dama blanca sobre el ser humano es digna de estudio."))
			.body("[3].id", equalTo(5))
			.body("[3].title", equalTo("LA LEGIÓN PERDIDA"))
			.body("[3].description", equalTo("En el año 53 a. C. el cónsul Craso cruzó el Éufrates para conquistar Oriente, pero su ejército fue destrozado en Carrhae. Una legión entera cayó prisionera de los partos. Nadie sabe a ciencia cierta qué pasó con aquella legión perdida.150 años después, Trajano está a punto de volver a cruzar el Éufrates. ..."));		
	}
	
	@Test
	public void createBookOk() throws JsonProcessingException {

		Response response = 
			given()
	            .auth()
	            .basic("user", "pass")
	            .contentType("application/json")
	            .body(objectMapper.writeValueAsString(book))
	            .when()
	            .post("/api/books/").andReturn();

        int id = from(response.getBody().asString()).get("id");

        when()
            .get("/api/books/{id}",id)
            .then()
            .statusCode(200)
            .body("title", equalTo("New Book"))
            .body("description", equalTo("New"));         
	}


	@Test
	public void deleteBookOk()  {

        given()
            .auth()
            .basic("admin", "pass")
        .when()
            .delete("/api/books/1")
            .then()
            .statusCode(200);
       
        given()
	        .auth()
	        .basic("admin", "pass")
	    .when()
	        .delete("/api/books/100")
	        .then()
	        .statusCode(404);
	}

}

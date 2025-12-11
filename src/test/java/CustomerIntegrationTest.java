import Entities.Customer;
import Repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringDataR2DBCApplication.class)
public class CustomerIntegrationTest {

    private static final String API_BASE_URL = "/customers";
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CustomerRepository repository;

    @BeforeEach
    public void setup() {
        repository.deleteAll().block();

        repository.saveAll(List.of(
                new Customer(null, "Pedro", "Kuhn"),
                new Customer(null, "Lia", "Kuhn")
        )).blockLast();
    }

    @Test
    void shouldGetAllCustomers() {
        webTestClient.get().uri(API_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Customer.class)
                .hasSize(2)
                .contains(new Customer(null, "Pedro", "Kuhn"))
                .contains(new Customer(null, "Lia", "Kuhn"));
    }

    @Test
    void shouldGetCustomerById() {
        Customer savedCustomer = repository.save(new Customer(null, "Carlos", "Mendes")).block();

        webTestClient.get().uri(API_BASE_URL + "/{id}", savedCustomer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(c -> {
                    assertEquals("Carlos", c.getFirstName());
                    assertEquals("Mendes", c.getLastName());
                });
    }

    @Test
    void shouldReturn404ForNonExistingCustomer() {
        webTestClient.get().uri(API_BASE_URL + "/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldCreateNewCustomer() {
        Customer newCustomer = new Customer(null, "Julia", "Silva");

        webTestClient.post().uri(API_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newCustomer), Customer.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer.class)
                .value(c -> {
                    assertEquals("Julia", c.getFirstName());
                    assertEquals("Silva", c.getLastName());
                    assertNotNull(c.getId());
                });
    }
}

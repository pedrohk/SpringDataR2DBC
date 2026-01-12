import Entities.Customer;
import Repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
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
                .contains(new Customer(null,"Pedro", "Kuhn"))
                .contains(new Customer( null,"Lia", "Kuhn"));
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

    @Test
    void shouldInsertAndReturnTenCustomersUsingFlux() {
        Flux<Customer> customersFlux = Flux.just(
                new Customer(null, "Customer1", "LastName1"),
                new Customer(null, "Customer2", "LastName2"),
                new Customer(null, "Customer3", "LastName3"),
                new Customer(null, "Customer4", "LastName4"),
                new Customer(null, "Customer5", "LastName5"),
                new Customer(null, "Customer6", "LastName6"),
                new Customer(null, "Customer7", "LastName7"),
                new Customer(null, "Customer8", "LastName8"),
                new Customer(null, "Customer9", "LastName9"),
                new Customer(null, "Customer10", "LastName10")
        );

        repository.saveAll(customersFlux).blockLast();

        webTestClient.get().uri(API_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Customer.class)
                .hasSize(12)
                .value(customers -> {
                    long count = customers.stream()
                            .filter(c -> c.getFirstName().startsWith("Customer"))
                            .count();
                    assertEquals(10, count, "Should have 10 customers with names starting with 'Customer'");
                });
    }
}

package Controller;


import Entities.Customer;
import Repository.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Flux<Customer> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Customer> getById(@PathVariable Long id) {

        return repository.findById(id);
    }

    @PostMapping
    public Mono<Customer> create(@RequestBody Customer customer) {
        return repository.save(customer);
    }
}
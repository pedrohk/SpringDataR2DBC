import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = {"Entities", "Controller", "Repository"})
@EnableR2dbcRepositories(basePackages = "CustomerRepository")
public class SpringDataR2DBCApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDataR2DBCApplication.class, args);
    }
}
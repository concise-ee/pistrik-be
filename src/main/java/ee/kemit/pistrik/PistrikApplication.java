package ee.kemit.pistrik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PistrikApplication {

  public static void main(String[] args) {
    SpringApplication.run(PistrikApplication.class, args);
  }
}

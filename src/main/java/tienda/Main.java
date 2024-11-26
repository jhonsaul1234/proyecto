package tienda;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tienda.ui.LoginUI;

@SpringBootApplication
@EntityScan("tienda.model")  // ajusta según tu paquete de entidades
@EnableJpaRepositories("tienda.repository")  // ajusta según tu paquete de repositorios
public class Main {
	public static void main(String[] args) {
		Application.launch(LoginUI.class, args);
	}
}
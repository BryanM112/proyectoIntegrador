package ec.edu.ups.icc.proyectointegrador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ec.edu.ups.icc.proyectointegrador.security.config.JwtProperties;


@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ProyectointegradorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectointegradorApplication.class, args);
	}

}

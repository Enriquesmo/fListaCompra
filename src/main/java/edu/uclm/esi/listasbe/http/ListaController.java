package edu.uclm.esi.listasbe.http;

import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.services.ListaService;
import jakarta.servlet.http.HttpServletRequest;

//En los controller, lo que se hace es comprobar que no hay ningun erroor de faltar datos o algo parecido para que una vez visto
//eso, se llame a la verdadera funcion, que se encuentra en services
//Recibe las peticiones del usuario, que tienen parametros y los obtienen para pasarelos a los services

@RestController
@RequestMapping("listas")
@CrossOrigin(origins = "https://localhost:4200",allowCredentials = "true")
public class ListaController {
	@Autowired
	private ListaService listaService;
	
	@PostMapping("/crearLista")
	public Lista crearLista(@RequestParam String nombre, @RequestParam String email) {
	    nombre = nombre.trim();
	    email = email.trim();

	    if (nombre.isEmpty())
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
	    
	    if (nombre.length() > 80)
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista está limitado a 80 caracteres");

	    return this.listaService.crearLista(nombre, email);
	}


	
	@PutMapping("/comprar")
	public Producto comprar(@RequestBody Map<String, Object>compra) {
		String idProducto = compra.get("idProducto").toString();
		float unidadesCompradas = (float) compra.get("unidadesCompradas");
		return null;
	}
	
	@GetMapping("/getListas")
	public List<Lista> getListas(@RequestParam String email){
		return this.listaService.getListas(email);
	}
	
	@PostMapping("/addInvitado")
	public String addInvitado(HttpServletRequest request,@RequestBody String email) {
		
		String idLista = request.getHeader("idLista");
		return this.listaService.addInvitado(idLista,email);
	}
	
	@PostMapping("/aceptarInvitacion")
	public void aceptarInvitacion(HttpServletRequest response,@RequestBody String email) {
	//this.listaService.aceptarInvitacion(idLista,email);
	//response.sendRedirect("https://localhost:4200");
	}
	
	
}


















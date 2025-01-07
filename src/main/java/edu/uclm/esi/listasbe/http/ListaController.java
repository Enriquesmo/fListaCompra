package edu.uclm.esi.listasbe.http;

import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import edu.uclm.esi.listasbe.services.ProxyBEU;
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
	
	@Autowired
	private ProxyBEU token;
	
	@PostMapping("/crearLista")
	public Lista crearLista(HttpServletRequest request,@RequestParam String nombre, @RequestParam String email) {
		
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
					int cant=this.listaService.cantidadListasTieneUser(email);
					boolean vip=this.listaService.vip(email);
					if(vip||cant<2) {
					  	nombre = nombre.trim();
					    email = email.trim();
					    if (nombre.isEmpty())
					        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
					    if (nombre.length() > 80)
					        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista está limitado a 80 caracteres");
					    return this.listaService.crearLista(nombre, email);
					}
					return null;
			 }
			 return null;
		 }
		 return null;
		
		
		

		
	}
	

	
	@GetMapping("/getListas")
	public List<Lista> getListas(HttpServletRequest request,@RequestParam String email){
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 return this.listaService.getListas(email);
			 }
			 return null;
		 }
		 return null;
	}
	
	

	@PostMapping("/accept-invitacion")
	public ResponseEntity<?> acceptInvitation(HttpServletRequest request,@RequestParam String idLista, @RequestParam String email) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 boolean permitirCompartir = this.listaService.permitirComp(idLista);
				    int cant = this.listaService.cantidadListasTieneUser(email);
				    boolean vip = this.listaService.vip(email);

				    if (permitirCompartir) {
				        if (vip || cant < 2) {
				            Lista lista = this.listaService.aceptarInvitacion(idLista, email);
				            return ResponseEntity.ok(lista);
				        } else {
				            return ResponseEntity.status(HttpStatus.FORBIDDEN)
				                .body(Map.of("status", "error", "message", "Necesitas ser premium para añadir más listas."));
				        }
				    }
				    return ResponseEntity.status(HttpStatus.FORBIDDEN)
				        .body(Map.of("status", "error", "message", "El usuario creador de la sala no puede añadir más personas."));
			 }
			 return ResponseEntity.status(HttpStatus.FORBIDDEN)
				        .body(Map.of("status", "error", "message", "Error."));
		 }
		 return ResponseEntity.status(HttpStatus.FORBIDDEN)
			        .body(Map.of("status", "error", "message", "Error."));
	}


	@PutMapping("/cambiarNombre")
	public Lista cambiarNombre(HttpServletRequest request, String idLista, String nuevoNombre) {
	    // Llamar al servicio para cambiar el nombre y devolver la lista actualizada
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				    return this.listaService.cambiarNombre(idLista, nuevoNombre); 
			 }
			 return null;
		 }
		 return null;
	}
	
	@DeleteMapping("/eliminarMiembro")
	public Lista eliminarMiembro(HttpServletRequest request,String email, String idLista) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 return this.listaService.eliminarMiembro(email,idLista);
			 }
			 return null;
		 }
		 return null;
	}
}


















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

import org.springframework.web.bind.annotation.DeleteMapping;



@RestController
@RequestMapping("listas")
@CrossOrigin(origins = "https://localhost:4200",allowCredentials = "true")
public class ListaController {
	@Autowired
	private ListaService listaService;
	
	@Autowired
	private ProxyBEU token;
	
	@PostMapping("/crearLista")
	public ResponseEntity<?> crearLista(HttpServletRequest request, @RequestParam String nombre, @RequestParam String email) {
	    String fakeUserId = token.findCookie(request, "fakeUserId");
	    if (fakeUserId != null) {
	        boolean validado = token.validar(fakeUserId);
	        if (validado) {
	        	try {
		            int cant = this.listaService.cantidadListasTieneUser(email);
		            boolean vip = this.listaService.vip(email);
		            if (vip || cant < 2) {
		                nombre = nombre.trim();
		                if (nombre.isEmpty()) {
		                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El nombre de la lista no puede estar vacío."));
		                }
		                if (nombre.length() > 80) {
		                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El nombre de la lista está limitado a 80 caracteres."));
		                }
		                Lista lista = this.listaService.crearLista(nombre, email);
		                return ResponseEntity.ok(lista);
		            }
		            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El usuario no puede crear más listas.Asegurese de ser vip o borre alguna lista."));
	        	}catch (ResponseStatusException e) {
	                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
	            }
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
	    }
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}

	@GetMapping("/getListas")
	public ResponseEntity<?> getListas(HttpServletRequest request,@RequestParam String email){
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 try {
					 List<Lista> lista= this.listaService.getListas(email);
					 return ResponseEntity.ok(lista); 
				 }catch (ResponseStatusException e) {
		                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
		            }
			 }
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
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
				            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status", "error", "message", "Necesitas ser premium para añadir más listas."));
				        }
				    }
				    return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("status", "error", "message", "El usuario creador de la sala no puede añadir más personas."));
			 }
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}


	@PutMapping("/cambiarNombre")
	public ResponseEntity<?> cambiarNombre(HttpServletRequest request, String idLista, String nuevoNombre) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 try {
					 Lista lista=this.listaService.cambiarNombre(idLista, nuevoNombre); 
					    return ResponseEntity.ok(lista); 
				 }catch (ResponseStatusException e) {
		                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
		            }
			 }
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}
	
	@DeleteMapping("/eliminarMiembro")
	public ResponseEntity<?> eliminarMiembro(HttpServletRequest request,String email, String idLista) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 try {
					 Lista lista=this.listaService.eliminarMiembro(email,idLista);
					 return ResponseEntity.ok(lista);
				 }catch (ResponseStatusException e) {
		                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
		            }
			 }
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}
	
	@DeleteMapping("/eliminarLista")
	public ResponseEntity<?> eliminarLista(HttpServletRequest request, @RequestParam String idLista, @RequestParam String email) {
	    String fakeUserId = token.findCookie(request, "fakeUserId");
	    if (fakeUserId != null) {
	        boolean validado = token.validar(fakeUserId);
	        if (validado) {
	            try {
	                this.listaService.eliminarLista(idLista, email);
	                return ResponseEntity.ok( "Lista Eliminada.");
	            } catch (ResponseStatusException e) {
	                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
	            }
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
	    }
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}
	
}


















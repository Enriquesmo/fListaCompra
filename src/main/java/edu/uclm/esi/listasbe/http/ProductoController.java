package edu.uclm.esi.listasbe.http;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import  edu.uclm.esi.listasbe.services.ProductoService;
import edu.uclm.esi.listasbe.services.ProxyBEU;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("productos")
@CrossOrigin(origins = "https://localhost:4200",allowCredentials = "true")
public class ProductoController {
	@Autowired
	private ProxyBEU token;
	@Autowired
	private ProductoService productoService;
	

	@PutMapping("/producto")
	public ResponseEntity<?> modificarProducto(HttpServletRequest request,@RequestBody Producto producto) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 try {
					 Producto productoDevolver=this.productoService.modifyProducto(producto);
					 return  ResponseEntity.ok( productoDevolver); 
				 }catch (ResponseStatusException e) {
		                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
		            }
			 }
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}

	@DeleteMapping("/producto")
	public ResponseEntity<?> delete(HttpServletRequest request, @RequestParam String idLista,@RequestParam String idProducto) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 try {
					 Lista lista= this.productoService.deleteProducto(idProducto,idLista);
					 return ResponseEntity.ok( lista);
				 }catch (ResponseStatusException e) {
		                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
		            }
			 }
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}
	
	@PostMapping("/producto")
	public ResponseEntity<?> addProducto(HttpServletRequest request,@RequestBody Producto producto) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
			try {
				 if (producto.getNombre().isEmpty())
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre no puede estar vacio");
					
					if (producto.getNombre().length()>80)
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre de la lista esta limitado a 80 caracteres");
					
					String idLista = request.getHeader("idLista");
					String email = request.getHeader("email");
					Lista lista= this.productoService.addProducto(idLista,producto,email);
					return ResponseEntity.ok( lista);
			}catch (ResponseStatusException e) {
                return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason())); 
            }
			 }
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Error al verificar su sesión."));
		 }
		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario no autenticado."));
	}
	
	
}

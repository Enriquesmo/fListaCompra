package edu.uclm.esi.listasbe.http;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	public Producto modificarProducto(HttpServletRequest request,@RequestBody Producto producto) {
		
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 return this.productoService.modifyProducto(producto); 
			 }
			 return null;
		 }
	    return null;
	}

	
	@GetMapping("/producto")
    public List<Producto> getProducto(HttpServletRequest request,@RequestParam String idLista) {
        // Llamar al servicio para obtener los productos de la lista por su id
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 return productoService.getProductosDeLista(idLista);
			 }
			 return null;
		 }
	    return null;
    }

	
	@DeleteMapping("/producto")
	public Lista delete(HttpServletRequest request, @RequestParam String idLista,@RequestParam String idProducto) {
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 return this.productoService.deleteProducto(idProducto,idLista);
			 }
			 return null;
		 }
	    return null;
	}
	
	@PostMapping("/producto")
	public Lista addProducto(HttpServletRequest request,@RequestBody Producto producto) {
		
		String fakeUserId = token.findCookie(request, "fakeUserId");
		 if (fakeUserId != null) {
			 boolean validado=token.validar(fakeUserId);
			 if (validado) {
				 if (producto.getNombre().isEmpty())
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre no puede estar vacio");
					
					if (producto.getNombre().length()>80)
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre de la lista esta limitado a 80 caracteres");
					
					String idLista = request.getHeader("idLista");
					String email = request.getHeader("email");
					return this.productoService.addProducto(idLista,producto,email);
			 }
			 return null;
		 }
		return null;
		
		
	}
	
	
}

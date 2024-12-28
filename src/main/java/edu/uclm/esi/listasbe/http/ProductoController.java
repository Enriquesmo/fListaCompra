package edu.uclm.esi.listasbe.http;

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
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("productos")
@CrossOrigin(origins = "https://localhost:4200",allowCredentials = "true")
public class ProductoController {
	
	@Autowired
	private ProductoService productoService;
	

	@PutMapping("/producto")
	public void modificarProducto(@RequestBody Producto producto) {
	
		this.productoService.modifyProducto(producto);
		
	}
	
	@GetMapping("/producto")
	public Lista getProducto() {

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"get producto");
	}
	

	
	@DeleteMapping("/producto")
	public void delete( @RequestParam String idLista,@RequestParam String idProducto) {
		
		this.productoService.deleteProducto(idProducto,idLista);
	}
	
	@PostMapping("/producto")
	public Lista addProducto(HttpServletRequest request,@RequestBody Producto producto) {
		if (producto.getNombre().isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre no puede estar vacio");
		
		if (producto.getNombre().length()>80)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre de la lista esta limitado a 80 caracteres");
		
		String idLista = request.getHeader("idLista");
		return this.productoService.addProducto(idLista,producto);
	}
	
	
}

package edu.uclm.esi.listasbe.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.ws.WSListas;

@Service
public class ProductoService {
	@Autowired
	private ListaDao listaDao;
	@Autowired
	private ProductoDao productoDao;
	@Autowired
	private WSListas wsListas;

	public Lista addProducto(String idLista, Producto producto) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		lista.add(producto);
		
		producto.setLista(lista);
		this.productoDao.save(producto);
		this.wsListas.notificar(idLista, producto);
		return lista;
	}

	public Lista deleteProducto(String idLista, Producto producto) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		lista.getProductos().stream().forEach((p)-> {
			if (p.getId().equalsIgnoreCase(producto.getId())) {
				lista.getProductos().remove(p);
			}
		});
		
		producto.setLista(lista);
		this.productoDao.delete(producto);
		this.wsListas.notificar(idLista, producto);
		return lista;
		
		/* ESTO ES LO MISMO QUE LO DE ARRIBA PERO LO DE ARRIBA ES MAS FISNO
		 * 
		for (Producto p : lista.getProductos()) {
			if (p.getId().equalsIgnoreCase(producto.getId())) {
				lista.getProductos().remove(p);
			}
			
		}*/
		
		
		

	}

	public Lista modifyProducto(Producto producto) {
		return null;
	}
}

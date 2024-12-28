package edu.uclm.esi.listasbe.services;

import java.util.Iterator;
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
		
		Producto productoGuardar = new Producto();
		productoGuardar.setNombre(producto.getNombre());
		productoGuardar.setUnidadesCompradas(producto.getUnidadesCompradas());
		productoGuardar.setUnidadesPedidas(producto.getUnidadesPedidas());
		productoGuardar.setLista(lista);
		lista.add(productoGuardar);
		this.productoDao.save(productoGuardar);
		this.wsListas.notificar(idLista, productoGuardar);
		return lista;
	}

	public void deleteProducto(String idProducto, String idLista) {
	    // Buscar el producto por su ID
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }

	    // Buscar la lista por su ID
	    //Optional<Lista> optLista = this.listaDao.findById(idLista);
	    //if (optLista.isEmpty()) {
	        //throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    //}

	    //Lista lista = optLista.get();
	    Producto producto = optProducto.get();

	    // Usar un iterador explícito para evitar ConcurrentModificationException
	    //Iterator<Producto> iterator = lista.getProductos().iterator();
	    //while (iterator.hasNext()) {
	        //Producto p = iterator.next();
	        //if (p.getId().equalsIgnoreCase(producto.getId())) {
	            //iterator.remove(); // Eliminación segura
	        //}
	    //}

	    // Actualizar el estado del producto y eliminarlo de la base de datos
	    //producto.setLista(lista);
	    this.productoDao.delete(producto);

	    // Notificar cambios
	    this.wsListas.notificar(idLista, producto);

	    //return lista;
	}


	public void modifyProducto(Producto producto) {
		String idProducto=producto.getId();
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }
	    Producto productoAGuardar = optProducto.get();
	    productoAGuardar.setUnidadesPedidas(producto.getUnidadesPedidas());
	    this.productoDao.save(productoAGuardar);
	}
}

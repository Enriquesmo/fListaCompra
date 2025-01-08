package edu.uclm.esi.listasbe.services;

import java.util.Iterator;
import java.util.List;
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
	@Autowired
	private ListaService listaService;
	
	public Lista addProducto(String idLista, Producto producto,String email) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		int cant=lista.getProductos().size();
		boolean vip=this.listaService.vip(email);
		if(vip||cant<10) {
			Producto productoGuardar = new Producto();
			productoGuardar.setNombre(producto.getNombre());
			productoGuardar.setUnidadesCompradas(producto.getUnidadesCompradas());
			productoGuardar.setUnidadesPedidas(producto.getUnidadesPedidas());
			productoGuardar.setLista(lista);
			lista.add(productoGuardar);
			this.productoDao.save(productoGuardar);
			//this.wsListas.notificar(idLista, productoGuardar);
			wsListas.enviarMensajeAUsuariosDeLista(idLista, "Se ha añadido un nuevo producto: " + productoGuardar.getNombre(),lista);
			return lista;
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Necesita ser vip para añadir más de 10 productos, borre alguno.");
	}

	public Lista deleteProducto(String idProducto, String idLista) {
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }
	    Lista lista = optLista.get();
	    Producto producto = optProducto.get();
	    
	    Iterator<Producto> iterator = lista.getProductos().iterator();
	    while (iterator.hasNext()) {
	        Producto p = iterator.next();
	        if (p.getId().equalsIgnoreCase(producto.getId())) {
	            iterator.remove(); 
	        }
	    }
	    producto.setLista(lista);
	    this.productoDao.delete(producto);
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "Se ha eliminado un producto: " + producto.getNombre(),lista);
	    return lista;
	}


	public Producto modifyProducto(Producto producto) {
	    String idProducto = producto.getId();
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }
	    Producto productoAGuardar = optProducto.get();
	    productoAGuardar.setUnidadesPedidas(producto.getUnidadesPedidas());
	    productoAGuardar.setUnidadesCompradas(producto.getUnidadesCompradas());
	    Lista lista = productoAGuardar.getLista();
	    if (lista == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no tiene lista asociada");
	    }
	    this.productoDao.save(productoAGuardar);
	    wsListas.enviarMensajeAUsuariosDeLista(lista.getId(), "Se ha editado el producto: " + productoAGuardar.getNombre(),lista);
	    return productoAGuardar;
	}

	


}

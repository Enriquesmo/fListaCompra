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
	
	
	public Lista addProducto(String idLista, Producto producto,String email) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		int cant=lista.getProductos().size();
		boolean vip=this.listaDao.esUsuarioVip(email);
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
		return null;
	}

	public Lista deleteProducto(String idProducto, String idLista) {
	    // Buscar el producto por su ID
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }

	    // Buscar la lista por su ID
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }

	    Lista lista = optLista.get();
	    Producto producto = optProducto.get();

	    // Usar un iterador explícito para evitar ConcurrentModificationException
	    Iterator<Producto> iterator = lista.getProductos().iterator();
	    while (iterator.hasNext()) {
	        Producto p = iterator.next();
	        if (p.getId().equalsIgnoreCase(producto.getId())) {
	            iterator.remove(); // Eliminación segura
	        }
	    }

	    // Actualizar el estado del producto y eliminarlo de la base de datos
	    producto.setLista(lista);
	    this.productoDao.delete(producto);

	    // Notificar cambios
	    //this.wsListas.notificar(idLista, producto);
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "Se ha eliminado un producto: " + producto.getNombre(),lista);
	    return lista;
	}


	public Producto modifyProducto(Producto producto) {
	    String idProducto = producto.getId();
	    Optional<Producto> optProducto = this.productoDao.findById(idProducto);
	    if (optProducto.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");
	    }
	    
	    // Obtenemos el producto existente
	    Producto productoAGuardar = optProducto.get();
	    
	    // Asignar los valores de las unidades del producto
	    productoAGuardar.setUnidadesPedidas(producto.getUnidadesPedidas());
	    productoAGuardar.setUnidadesCompradas(producto.getUnidadesCompradas());
	    
	    // Obtener la lista correspondiente al producto (ya que el producto tiene un campo lista)
	    Lista lista = productoAGuardar.getLista();
	    if (lista == null) {
	        // Si el producto no tiene lista asignada, lanzamos una excepción (o puedes manejarlo de otra manera)
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no tiene lista asociada");
	    }
	    
	    // Guardamos el producto actualizado
	    this.productoDao.save(productoAGuardar);
	    wsListas.enviarMensajeAUsuariosDeLista(lista.getId(), "Se ha editado el producto: " + productoAGuardar.getNombre(),lista);
	    // Devuelve el producto modificado (aunque podría también devolver la lista si es necesario)
	    return productoAGuardar;
	}

	
	public List<Producto> getProductosDeLista(String idLista) {
	    // Buscar la lista por su ID
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    
	    // Si la lista no existe, lanzar un error
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }
	    
	    // Obtener la lista
	    Lista lista = optLista.get();
	    
	    // Devolver los productos asociados a esa lista
	    return lista.getProductos();
	}

}

package edu.uclm.esi.listasbe.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.ws.WSListas;



@Service
public class ListaService {
		
	@Autowired 
	private ListaDao listaDao;
	@Autowired 
	private ProductoDao productoDao;
	@Autowired
	private ProxyBEU proxy;
	@Autowired
	private WSListas wsListas;
	
	public Lista crearLista(String nombre, String email) {
		Lista lista = new Lista();
		lista.setNombre(nombre);
		lista.setCreador(email);
		lista.addEmailUsuario(email);
	    String token = UUID.randomUUID().toString();
	    lista.setInvitation_token(token);
		this.listaDao.save(lista);
		return lista;
	}

	public List<Lista> getListas(String email) {
		List<Lista> result = new ArrayList<>();
		List<String> ids = this.listaDao.getListasDe(email);
		if(ids.size()==0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentran listas");
		}
		for(String id : ids) {
			result.add(this.listaDao.findById(id).get());
		}
		return result;
	}

	
	public Lista aceptarInvitacion(String listaId,  String emailUsuario) {
		Optional<Lista> optlista = this.listaDao.findById(listaId);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
	    if (lista.getEmailsUsuarios().contains(emailUsuario)) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es miembro de esta lista");
	    }

	    lista.addEmailUsuario(emailUsuario);
	    listaDao.save(lista);
	    wsListas.enviarMensajeAUsuariosDeLista(listaId, "Nuevo Miembro: " + emailUsuario,lista);
	    return lista;
	}

	public Lista cambiarNombre(String idLista, String nuevoNombre) {
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }
	    Lista lista = optLista.get();
	    lista.setNombre(nuevoNombre);	   
	    this.listaDao.save(lista);
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "El nombre de la lista ha cambiado a: " + nuevoNombre,lista);
	    return lista;
	}
	
	public boolean vip(String email) {
	    boolean vip = this.listaDao.esUsuarioVip(email);  
	    if(vip) {
		    boolean fecha = this.listaDao.VipDate(email).isAfter(LocalDateTime.now());  
		    boolean valido=false;
		    if(vip&&fecha){
		    	valido=true;
		    }
		    if(!fecha) {
		    	
		    	this.listaDao.desactivarVip(email);
		    	 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Su sesión premium ha caducado");
		    }
		    return valido;
	    }
	    return false;
	}

	public int cantidadListasTieneUser (String email) {
		int cant=this.listaDao.contarListasDeUsuario(email);
		return cant;
	}
	
	public boolean permitirComp(String idLista) {
		boolean permitir=false;
		Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    } 
	    Lista lista = optLista.get();
	    int participantes=lista.getEmailsUsuarios().size();
	    String email=lista.getCreador();
	    boolean vip=vip(email);
	    if(vip||participantes<=1) {
	    	permitir=true;
	    }else {
	    	throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El creador de la lista debe ser vip o dejar de compartir con otro participante");
	    }
	    return permitir;
	}
	
	public Lista eliminarMiembro(String email, String idLista) {
	    
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    } 
	    Lista lista = optLista.get();
	    List<String> usuarios = lista.getEmailsUsuarios();	   
	    if (!usuarios.contains(email)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no pertenece a esta lista");
	    }
	    usuarios.remove(email);
	    this.listaDao.eliminarUsuarioDeLista(idLista, email); 
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "Se ha eliminado de la lista a: " + email,lista);
	    return lista;
	}
	
	public void eliminarLista(String idLista, String email) {
	    System.out.println("Intentando eliminar lista con ID: " + idLista + " y email: " + email); 
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        System.out.println("Lista no encontrada");
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La lista no existe");
	    }
	    Lista lista = optLista.get();
	    System.out.println("Lista encontrada: " + lista);
	    if (lista.getCreador() == null || !lista.getCreador().equals(email)) {
	        throw new IllegalArgumentException("El creador de la lista no está definido o no coincide.");
	    }
	    if (!lista.getCreador().equals(email)) {
	        System.out.println("El usuario no tiene permisos para eliminar esta lista");
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar esta lista");
	    }
	    System.out.println("Eliminando lista...");
	    this.listaDao.delete(lista);
	    System.out.println("Lista eliminada exitosamente");
	}

}















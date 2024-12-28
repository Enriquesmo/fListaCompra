package edu.uclm.esi.listasbe.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

//los services se usan para obtener o modificar o eliminar informacion de la BBDD
//Interacciona con la BBDD a traves de las clases DAO

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
		for(String id : ids) {
			result.add(this.listaDao.findById(id).get());
		}
		return result;
	}

	/*public String addInvitado(String idLista, String email) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		lista.addEmailUsuario(email);
		this.listaDao.save(lista);
		String url = "https://localhost:8443/listas/aceptarInvitacion?email=" + email + "&lista=" + idLista;
		return url;
	}*/
	
	public ResponseEntity<Object> aceptarInvitacion(String listaId, String token, String emailUsuario) {
	    Lista lista = listaDao.findById(listaId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
	    
	    if (!lista.getInvitation_token().equals(token)) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "El token de la lista no es coincidente");
	    }
	    // Verifica si el usuario ya está en la lista
	    if (lista.getEmailsUsuarios().contains(emailUsuario)) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es miembro de esta lista");
	    }

	    lista.addEmailUsuario(emailUsuario);
	    listaDao.save(lista);

	    return ResponseEntity.ok().build();
	}
	
	public ResponseEntity<String> generar_invitacion(String listaId) {
	    Lista lista = listaDao.findById(listaId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
	    String invitationLink = "https://localhost:4200/invitacion/" + "?token=" + lista.getInvitation_token();
	    return ResponseEntity.ok(invitationLink);
	}
	public void cambiarNombre(String idLista, String nuevoNombre) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		lista.setNombre(nuevoNombre);
		this.listaDao.save(lista);
	}
}















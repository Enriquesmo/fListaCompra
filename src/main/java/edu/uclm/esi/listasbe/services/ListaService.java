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
	
	public Lista aceptarInvitacion(String listaId,  String emailUsuario) {
		Optional<Lista> optlista = this.listaDao.findById(listaId);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
	   // if (!lista.getInvitation_token().equals(token)) {
	     //   throw new ResponseStatusException(HttpStatus.CONFLICT, "El token de la lista no es coincidente");
	   // }
	    // Verifica si el usuario ya está en la lista
	    if (lista.getEmailsUsuarios().contains(emailUsuario)) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es miembro de esta lista");
	    }

	    lista.addEmailUsuario(emailUsuario);
	    listaDao.save(lista);
	    wsListas.enviarMensajeAUsuariosDeLista(listaId, "Nuevo Miembro: " + emailUsuario,lista);
	    return lista;
	}
	
	//public ResponseEntity<String> generar_invitacion(String listaId) {
	  //  Lista lista = listaDao.findById(listaId)
	    //        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
	   // String invitationLink = "https://localhost:4200/invitacion/" + "?token=" + lista.getInvitation_token();
	   // return ResponseEntity.ok(invitationLink);
	//}
	public Lista cambiarNombre(String idLista, String nuevoNombre) {
	    // Buscar la lista por su ID
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }

	    // Obtener la lista y actualizar el nombre
	    Lista lista = optLista.get();
	    lista.setNombre(nuevoNombre);

	    // Guardar la lista con el nuevo nombre
	    this.listaDao.save(lista);
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "El nombre de la lista ha cambiado a: " + nuevoNombre,lista);
	    // Devolver la lista con el nuevo nombre
	    return lista;
	}
	public boolean vip(String email) {
	    boolean vip = this.listaDao.esUsuarioVip(email);  // Verificamos si el usuario es VIP
	    boolean fecha = this.listaDao.VipDate(email).isAfter(LocalDateTime.now());  // Verificamos si la fecha es posterior a la actual
	    boolean valido=false;
	    if(vip&&fecha){
	    	valido=true;// Si es VIP y la fecha es posterior (es decir, válida), devolvemos true
	    }
	    if(!fecha) {
	    	this.listaDao.desactivarVip(email);
	    }
	    return valido;
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

	    // Obtener la lista y actualizar el nombre
	    Lista lista = optLista.get();
	    int participantes=lista.getEmailsUsuarios().size();
	    String email=lista.getCreador();
	    boolean vip=vip(email);
	    if(vip||participantes<=1) {
	    	permitir=true;
	    }
	    return permitir;
	}
	
	public Lista eliminarMiembro(String email, String idLista) {
	    // Verificar si la lista existe
	    Optional<Lista> optLista = this.listaDao.findById(idLista);
	    if (optLista.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");
	    }

	    // Obtener la lista
	    Lista lista = optLista.get();
	    
	    // Obtener los usuarios asociados a la lista
	    List<String> usuarios = lista.getEmailsUsuarios();

	    // Verificar si el usuario está en la lista
	    if (!usuarios.contains(email)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no pertenece a esta lista");
	    }

	    // Eliminar el usuario de la lista
	    usuarios.remove(email);

	    // Actualizar la base de datos
	    this.listaDao.eliminarUsuarioDeLista(idLista, email); // Método en ListaDao definido previamente
	    wsListas.enviarMensajeAUsuariosDeLista(idLista, "Se ha eliminado de la lista a: " + email,lista);
	    // Devolver la lista actualizada
	    return lista;
	}

}















package edu.uclm.esi.listasbe.services;

import java.util.ArrayList;
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
	
	public Lista crearLista(String nombre,String token) {
		//String email = this.proxy.validar(token); //tiene que devolver un email
		//if (email == null) {
			//throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		//}
		
		
		
		Lista lista = new Lista();
		//lista.setNombre(nombre);
		//lista.addEmailUsuario(email);
		//this.listaDao.save(lista);
		//lista.confirmar(lista.getId(),email);
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

	public String addInvitado(String idLista, String email) {
		Optional<Lista> optlista = this.listaDao.findById(idLista);
		if (optlista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		Lista lista = optlista.get();
		lista.addEmailUsuario(email);
		this.listaDao.save(lista);
		String url = "https://localhost:8443/listas/aceptarInvitacion?email=" + email + "&lista=" + idLista;
		return url;
	}
	
	public void aceptarInvitacion(String idLista, String email) {
		this.listaDao.confirmar(idLista,email);
	}
	
}















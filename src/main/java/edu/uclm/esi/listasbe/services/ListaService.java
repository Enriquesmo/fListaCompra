package edu.uclm.esi.listasbe.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

	    return lista;
	}
	
	public ResponseEntity<String> generar_invitacion(String listaId) {
	    Lista lista = listaDao.findById(listaId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
	    String invitationLink = "https://localhost:4200/invitacion/" + "?token=" + lista.getInvitation_token();
	    return ResponseEntity.ok(invitationLink);
	}
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
		boolean vip=false;
		vip=this.listaDao.esUsuarioVip(email);
		return vip;
	}
	public int cantidadCreadas (String email) {
		int cant=this.listaDao.contarListasDeUsuario(email);
		return cant;
	}
	
/**	public boolean vip(String email) {
	    String url = "https://localhost:9000/users/verificar-vip?email=" + email; // URL del backend de usuarios

	    try {
	        // Crear la URL
	        URL obj = new URL(url);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	        // Establecer el método de la solicitud
	        con.setRequestMethod("GET");

	        // Obtener el código de respuesta
	        int responseCode = con.getResponseCode();
	        System.out.println("Response Code: " + responseCode);

	        // Si la respuesta es OK (200), leer la respuesta
	        if (responseCode == HttpURLConnection.HTTP_OK) { // 200
	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuilder response = new StringBuilder();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();

	            // Ahora procesamos la respuesta
	            // Suponemos que la respuesta es un JSON o un valor simple booleano
	            // Si es un valor JSON, necesitarías parsear el JSON
	            // Ejemplo si la respuesta fuera "true" o "false":
	            if ("true".equals(response.toString())) {
	                return true;  // El usuario es VIP
	            } else {
	                return false; // El usuario no es VIP
	            }
	        } else {
	            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al verificar el estado VIP");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al conectar con el servicio de usuarios");
	    }
	}

    public boolean vip(String email) {
        String url = "http://localhost:9000/users/verificar-vip?email=" + email;  // Suponiendo que este es el endpoint para verificar VIP

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Realizamos la solicitud GET al backend de usuarios
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");  // Especificamos el tipo de contenido como JSON

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Si el código de estado es 200 OK, significa que el usuario es VIP
                return response.getCode() == 200; 
            }
        } catch (Exception e) {
            e.printStackTrace();  // Si ocurre un error, lo imprimimos
            return false; // Si hay algún error, se considera que no es VIP
        }
    }**/
}















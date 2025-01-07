package edu.uclm.esi.listasbe.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;

@Component
public class WSListas extends TextWebSocketHandler {
	@Autowired
	private ListaDao listaDao;
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    

	
	private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId());
        String email = this.getParameter(session, "email");
        List<String> listas = this.listaDao.getListasDe(email); // Método para obtener listas asociadas al usuario.
        
        for (String idLista : listas) {
        	System.out.println(idLista);
        	System.out.println(this.sessionsByIdLista.get(idLista));
            List<WebSocketSession> auxi = this.sessionsByIdLista.get(idLista);
            if (auxi == null) {
                auxi = new ArrayList<>();
                auxi.add(session);
            } else {
                auxi.add(session);
            }
            this.sessionsByIdLista.put(idLista, auxi);
        }
    }

	private String getParameter(WebSocketSession session, String parName) {
		URI uri =session.getUri();
		String query = uri.getQuery();
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			if (pair.length > 1 && parName.equals(pair[0])) {
				return pair[1];
			}
		}
		return null;
	}
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Error en la conexión WebSocket: " + exception.getMessage());
        this.sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Eliminar la sesión de sessionsByIdLista
        for (Map.Entry<String, List<WebSocketSession>> entry : sessionsByIdLista.entrySet()) {
            List<WebSocketSession> sessionList = entry.getValue();
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessionsByIdLista.remove(entry.getKey());
            }
        }
        // Eliminar la sesión de la lista global
        this.sessions.remove(session.getId());
        System.out.println("Conexión cerrada para sessionId: " + session.getId());
    }




    // Método para enviar mensajes a todos los miembros de una lista
    public void enviarMensajeAUsuariosDeLista(String listaId, String mensaje, Lista lista) {
        System.out.println("Enviando mensaje a los usuarios de la lista: " + listaId);
        System.out.println("Sesiones registradas: " + sessionsByIdLista.size());

        // Crear el mensaje JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mensaje", mensaje);
        jsonObject.put("listaId", listaId);

        // Crear un objeto JSON para representar la lista completa
        JSONObject listaJson = new JSONObject();
        listaJson.put("id", lista.getId());
        listaJson.put("nombre", lista.getNombre());
        listaJson.put("creador", lista.getCreador());
        listaJson.put("invitation_token", lista.getInvitation_token());
        listaJson.put("emailsUsuarios", lista.getEmailsUsuarios());

        // Convertir los productos a un JSONArray
        JSONArray productosJson = new JSONArray();
        for (Producto producto : lista.getProductos()) {
            JSONObject productoJson = new JSONObject();
            productoJson.put("id", producto.getId());
            productoJson.put("nombre", producto.getNombre());
            productoJson.put("unidadesPedidas", producto.getUnidadesPedidas());
            productoJson.put("unidadesCompradas", producto.getUnidadesCompradas());
            productosJson.put(productoJson);
        }
        listaJson.put("productos", productosJson);

        // Añadir la lista al mensaje principal
        jsonObject.put("lista", listaJson);

        // Convertir el mensaje completo a JSON String
        String mensajeJson = jsonObject.toString();

        // Obtener las sesiones asociadas a la lista
        List<WebSocketSession> listaSesiones = sessionsByIdLista.get(listaId);

        if (listaSesiones != null) {
            // Enviar el mensaje a todas las sesiones asociadas a la lista
            for (WebSocketSession session : listaSesiones) {
                try {
                    session.sendMessage(new TextMessage(mensajeJson));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No hay sesiones asociadas con el ID de lista: " + listaId);
        }
    }




}

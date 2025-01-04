package edu.uclm.esi.listasbe.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;

@Component
public class WSListas extends TextWebSocketHandler {

    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri.getQuery();
        Map<String, String> params = splitQuery(query);
        
        String email = params.get("email");
        String listaId = params.get("listaId");

        if (email != null && listaId != null) {
            this.sessions.put(session.getId(), session);
            String mensaje="Enviado";
            //this.enviarMensajeAUsuariosDeLista(listaId,mensaje,null );
            System.out.println("Conexión establecida para email: " + email + " y listaId: " + listaId);
        } else {
            System.out.println("Parámetros WebSocket inválidos.");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Error en la conexión WebSocket: " + exception.getMessage());
        this.sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.sessions.remove(session.getId());
        System.out.println("Conexión cerrada para sessionId: " + session.getId());
    }

    private Map<String, String> splitQuery(String query) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                           URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
        }
        return queryPairs;
    }

    // Método para enviar mensajes a todos los miembros de una lista
    public void enviarMensajeAUsuariosDeLista(String listaId, String mensaje, Lista lista) {
        System.out.println("Enviando mensaje a los usuarios de la lista: " + listaId);
        System.out.println("Sesiones registradas: " + sessions.size());

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

        // Enviar el mensaje a todas las sesiones asociadas a la lista
        for (WebSocketSession session : sessions.values()) {
            if (isUsuarioEnLista(session, listaId)) {
                try {
                    session.sendMessage(new TextMessage(mensajeJson));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Método para verificar si el usuario está en la lista (esto depende de cómo guardas los datos)
    private boolean isUsuarioEnLista(WebSocketSession session, String listaId) {
        // Lógica de validación para asociar la sesión con una lista
        return true;  // Devuelve true si el usuario está en la lista (mejorar lógica aquí)
    }
}

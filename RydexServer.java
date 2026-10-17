import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RydexServer {

    private static final int PORT = 8081;
    private static final VehicleManager vehicleManager = new VehicleManager();

   public static void main(String[] args) throws IOException {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Bind explicitly to 0.0.0.0 so external cloud traffic can reach it
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/fares", new FareHandler());
        server.createContext("/api/book", new BookingHandler());
        server.createContext("/api/booking", new StatusHandler());

        server.setExecutor(null);
        System.out.println("=================================================");
        System.out.println(" Rydex Server running on port: " + port);
        System.out.println("=================================================");
        server.start();
    }
    // Serve index.html
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("index.html");
            if (!file.exists()) {
                String error = "<h1>500: index.html not found in project directory</h1>";
                exchange.sendResponseHeaders(404, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
                return;
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // POST /api/fares -> computes price for Bike, Auto, Cab
    static class FareHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange.getRequestBody());
            double dist = Double.parseDouble(params.getOrDefault("distance", "5.0"));

            Map<String, Object> fares = vehicleManager.calculateAllFares(dist);
            String jsonResponse = toJson(fares);

            sendJsonResponse(exchange, 200, jsonResponse);
        }
    }

    // POST /api/book -> reserves ride, returns driver details and OTP
    static class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> p = parseBody(exchange.getRequestBody());
            String type = p.getOrDefault("type", "BIKE");
            String pickup = p.getOrDefault("pickup", "Supela Chowk");
            String dropoff = p.getOrDefault("dropoff", "Durg Railway Station");
            double pLat = Double.parseDouble(p.getOrDefault("pickupLat", "21.2032"));
            double pLng = Double.parseDouble(p.getOrDefault("pickupLng", "81.3394"));
            double dLat = Double.parseDouble(p.getOrDefault("dropLat", "21.1904"));
            double dLng = Double.parseDouble(p.getOrDefault("dropLng", "81.2849"));
            double distance = Double.parseDouble(p.getOrDefault("distance", "0"));

            VehicleManager.Booking booking = vehicleManager.createBooking(type, pickup, dropoff, pLat, pLng, dLat, dLng, distance);

            String response = String.format(
                    "{\"bookingId\":\"%s\",\"status\":\"%s\",\"vehicle\":\"%s\",\"fare\":%.2f,\"otp\":\"%s\"," +
                            "\"distance\":%.2f,\"driver\":{\"name\":\"%s\",\"phone\":\"%s\",\"plate\":\"%s\",\"rating\":%.1f,\"lat\":%.4f,\"lng\":%.4f}}",
                    booking.bookingId, booking.status, booking.vehicleType, booking.totalFare, booking.otp,
                    booking.distanceKm, booking.assignedDriver.name, booking.assignedDriver.phone,
                    booking.assignedDriver.vehicleNumber, booking.assignedDriver.rating,
                    booking.assignedDriver.lat, booking.assignedDriver.lng
            );

            sendJsonResponse(exchange, 200, response);
        }
    }

    // GET /api/booking?id=XYZ
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String query = exchange.getRequestURI().getQuery();
            String id = query != null && query.startsWith("id=") ? query.substring(3) : "";

            VehicleManager.Booking b = vehicleManager.getBooking(id);
            if (b == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Booking not found\"}");
                return;
            }

            String response = String.format("{\"bookingId\":\"%s\",\"status\":\"%s\",\"fare\":%.2f}", b.bookingId, b.status, b.totalFare);
            sendJsonResponse(exchange, 200, response);
        }
    }

    // Simple JSON helpers without external libraries
    private static void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseBody(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        String raw = sb.toString().trim();

        Map<String, String> map = new HashMap<>();
        if (raw.startsWith("{") && raw.endsWith("}")) {
            raw = raw.substring(1, raw.length() - 1);
            String[] pairs = raw.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof Map) {
                sb.append(toJson((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof Number) {
                sb.append(entry.getValue());
            } else {
                sb.append("\"").append(entry.getValue()).append("\"");
            }
            if (++i < map.size()) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}

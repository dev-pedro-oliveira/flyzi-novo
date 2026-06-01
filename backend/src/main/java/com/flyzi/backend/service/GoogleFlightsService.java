package com.flyzi.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleFlightsService {

    private static final String RAPIDAPI_KEY = "69e04958c4msh223796dab55e40ap10f77ajsn599fb6045336";
    private static final String RAPIDAPI_HOST = "google-flights1.p.rapidapi.com";
    private static final String BASE_URL = "https://google-flights1.p.rapidapi.com/searchFlights";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Busca voos usando Google Flights
     */
    public List<Map<String, Object>> buscarVoos(String origem, String destino, String dataIda, String dataVolta) {
        List<Map<String, Object>> voos = new ArrayList<>();

        try {
            String dataIdaFormatada = converterData(dataIda);

            String url = String.format(
                "%s?departure_id=%s&arrival_id=%s&departure_date=%s&return_date=%s&currency=BRL&hl=pt-BR",
                BASE_URL, origem, destino, dataIdaFormatada, converterData(dataVolta)
            );

            System.out.println("🔍 Buscando Google Flights: " + origem + " → " + destino);

            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            
            request.setHeader("x-rapidapi-key", RAPIDAPI_KEY);
            request.setHeader("x-rapidapi-host", RAPIDAPI_HOST);

            client.execute(request, response -> {
                int statusCode = response.getCode();
                System.out.println("📊 Status: " + statusCode);
                
                if (statusCode != 200) {
                    System.err.println("❌ Erro HTTP: " + statusCode);
                    return null;
                }

                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);

                try {
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                    
                    if (!jsonObject.get("status").getAsBoolean()) {
                        System.out.println("⚠️ API retornou status false");
                        return null;
                    }

                    JsonObject dataObj = jsonObject.getAsJsonObject("data");
                    
                    if (dataObj == null || !dataObj.has("itineraries")) {
                        System.out.println("⚠️ Nenhum itinerário encontrado");
                        return null;
                    }

                    JsonObject itinerariesObj = dataObj.getAsJsonObject("itineraries");
                    
                    // Procurar por topFlights ou a primeira chave
                    JsonArray topFlights = null;
                    
                    if (itinerariesObj.has("topFlights")) {
                        topFlights = itinerariesObj.getAsJsonArray("topFlights");
                    } else {
                        // Pegar a primeira chave (ex: "0")
                        String firstKey = itinerariesObj.keySet().iterator().next();
                        JsonElement elem = itinerariesObj.get(firstKey);
                        if (elem.isJsonArray()) {
                            topFlights = elem.getAsJsonArray();
                        }
                    }

                    if (topFlights != null && topFlights.size() > 0) {
                        System.out.println("✅ Encontrados " + topFlights.size() + " voos");

                        for (JsonElement element : topFlights) {
                            JsonObject flight = element.getAsJsonObject();
                            
                            try {
                                Map<String, Object> voo = extrairDadosVoo(flight, origem, destino);
                                if (voo != null) {
                                    voos.add(voo);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Erro ao extrair voo: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("⚠️ Nenhum voo em topFlights");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erro ao parsear JSON: " + e.getMessage());
                    e.printStackTrace();
                }

                return null;
            });

            System.out.println("✅ Total de voos encontrados: " + voos.size() + "\n");

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar voos: " + e.getMessage());
            e.printStackTrace();
        }

        return voos;
    }

    /**
     * Extrai dados de um voo
     */
    private Map<String, Object> extrairDadosVoo(JsonObject flight, String origem, String destino) {
        try {
            Map<String, Object> voo = new HashMap<>();

            double preco = 0.0;
            if (flight.has("price")) {
                String priceStr = flight.get("price").getAsString();
                // Remove símbolos de moeda e espaços
                priceStr = priceStr.replaceAll("[^0-9.,]", "").trim();
                priceStr = priceStr.replace(".", "").replace(",", ".");
                try {
                    preco = Double.parseDouble(priceStr);
                } catch (Exception e) {
                    preco = 350.0;
                }
            }

            String companhia = "Desconhecida";
            if (flight.has("airline")) {
                companhia = mapearCompanhia(flight.get("airline").getAsString());
            }

            String dataPartida = "N/A";
            String horarioPartida = "N/A";
            String horarioChegada = "N/A";
            int duracao = 0;
            int stopover = 0;

            if (flight.has("departure_time")) {
                String depTime = flight.get("departure_time").getAsString();
                String[] parts = depTime.split(" ");
                if (parts.length >= 2) {
                    dataPartida = parts[0];
                    horarioPartida = parts[1];
                }
            }

            if (flight.has("arrival_time")) {
                String arrTime = flight.get("arrival_time").getAsString();
                String[] parts = arrTime.split(" ");
                if (parts.length >= 2) {
                    horarioChegada = parts[1];
                }
            }

            if (flight.has("duration")) {
                JsonObject durationObj = flight.getAsJsonObject("duration");
                if (durationObj.has("raw")) {
                    duracao = durationObj.get("raw").getAsInt() * 60; // Converter para segundos
                }
            }

            if (flight.has("stops")) {
                stopover = flight.get("stops").getAsInt();
            }

            int horas = duracao / 3600;
            int minutos = (duracao % 3600) / 60;
            String duracaoTexto = horas + "h " + minutos + "m";

            String tipo = stopover == 0 ? "Direto" : stopover + " Escala(s)";

            long milhas = (long) (preco * 4.0);

            voo.put("origem", origem);
            voo.put("destino", destino);
            voo.put("preco", preco);
            voo.put("companhia", companhia);
            voo.put("data", dataPartida);
            voo.put("horario", horarioPartida + " - " + horarioChegada);
            voo.put("duracaoTexto", duracaoTexto);
            voo.put("duracaoMinutos", duracao / 60);
            voo.put("tipo", tipo);
            voo.put("milhasNum", (int) milhas);
            voo.put("milhasFormatado", String.format("%,d", milhas).replace(",", "."));
            voo.put("teveQueda", false);
            voo.put("continente", "Brasil");
            voo.put("categoria", "Doméstico");

            return voo;

        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair voo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mapeia nome de companhia
     */
    private String mapearCompanhia(String nome) {
        if (nome == null) return "Desconhecida";
        
        nome = nome.toUpperCase();
        
        if (nome.contains("LATAM")) return "LATAM";
        if (nome.contains("GOL")) return "Gol";
        if (nome.contains("AZUL")) return "Azul";
        if (nome.contains("TAP")) return "TAP Air Portugal";
        if (nome.contains("AMERICAN")) return "American Airlines";
        if (nome.contains("UNITED")) return "United Airlines";
        
        return nome;
    }

    /**
     * Converte data de dd/MM/yyyy para dd-MM-yyyy
     */
    private String converterData(String data) {
        if (data == null || data.isEmpty()) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
        try {
            String[] partes = data.split("/");
            return partes[0] + "-" + partes[1] + "-" + partes[2];
        } catch (Exception e) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
    }
}
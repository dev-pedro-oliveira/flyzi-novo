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
public class SkyscannerService {

    private static final String RAPIDAPI_KEY = "69e04958c4msh223796dab55e40ap10f77ajsn599fb6045336";
    private static final String RAPIDAPI_HOST = "skyscanner-flights4.p.rapidapi.com";
    private static final String BASE_URL = "https://skyscanner-flights4.p.rapidapi.com/api/v1/searchFlights";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Busca voos usando Skyscanner
     */
    public List<Map<String, Object>> buscarVoos(String origem, String destino, String dataIda, String dataVolta) {
        List<Map<String, Object>> voos = new ArrayList<>();

        try {
            String dataIdaFormatada = converterDataParaISO(dataIda);
            String dataVoltaFormatada = converterDataParaISO(dataVolta);

            String url = String.format(
                "%s?fromEntityId=%s&toEntityId=%s&departDate=%s&returnDate=%s&limit=10",
                BASE_URL, origem, destino, dataIdaFormatada, dataVoltaFormatada
            );

            System.out.println("🔍 Buscando Skyscanner: " + origem + " → " + destino);

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
                    
                    JsonArray itineraries = null;
                    if (jsonObject.has("itineraries")) {
                        itineraries = jsonObject.getAsJsonArray("itineraries");
                    } else if (jsonObject.has("data")) {
                        itineraries = jsonObject.getAsJsonArray("data");
                    }

                    if (itineraries != null && itineraries.size() > 0) {
                        System.out.println("✅ Encontrados " + itineraries.size() + " itinerários");

                        for (JsonElement element : itineraries) {
                            JsonObject itinerary = element.getAsJsonObject();
                            
                            try {
                                Map<String, Object> voo = extrairDadosVoo(itinerary, origem, destino);
                                if (voo != null) {
                                    voos.add(voo);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Erro ao extrair voo: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("⚠️ Nenhum itinerário encontrado");
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
    private Map<String, Object> extrairDadosVoo(JsonObject itinerary, String origem, String destino) {
        try {
            Map<String, Object> voo = new HashMap<>();

            double preco = 0.0;
            if (itinerary.has("price")) {
                JsonObject priceObj = itinerary.getAsJsonObject("price");
                if (priceObj.has("raw")) {
                    preco = priceObj.get("raw").getAsDouble();
                }
            }

            // Converter para BRL
            double precoBRL = preco > 0 ? preco * 5.0 : 350.0;

            String companhia = "Desconhecida";
            if (itinerary.has("legs")) {
                JsonArray legs = itinerary.getAsJsonArray("legs");
                if (legs.size() > 0) {
                    JsonObject leg = legs.get(0).getAsJsonObject();
                    if (leg.has("carrierIds") && leg.getAsJsonArray("carrierIds").size() > 0) {
                        companhia = leg.getAsJsonArray("carrierIds").get(0).getAsString();
                        companhia = mapearCompanhia(companhia);
                    }
                }
            }

            String dataPartida = "N/A";
            String horarioPartida = "N/A";
            int duracao = 0;
            int stopover = 0;

            if (itinerary.has("legs")) {
                JsonArray legs = itinerary.getAsJsonArray("legs");
                if (legs.size() > 0) {
                    JsonObject leg = legs.get(0).getAsJsonObject();
                    
                    if (leg.has("departure")) {
                        String departure = leg.get("departure").getAsString();
                        String[] partes = departure.split("T");
                        if (partes.length > 0) {
                            dataPartida = partes[0].substring(8) + "/" + partes[0].substring(5, 7) + "/" + partes[0].substring(0, 4);
                        }
                        if (partes.length > 1) {
                            horarioPartida = partes[1].substring(0, 5);
                        }
                    }
                    
                    if (leg.has("durationInMinutes")) {
                        duracao = leg.get("durationInMinutes").getAsInt() * 60;
                    }
                    
                    if (leg.has("stopoverCount")) {
                        stopover = leg.get("stopoverCount").getAsInt();
                    }
                }
            }

            int horas = duracao / 3600;
            int minutos = (duracao % 3600) / 60;
            String duracaoTexto = horas + "h " + minutos + "m";

            String tipo = stopover == 0 ? "Direto" : stopover + " Escala(s)";

            long milhas = (long) (precoBRL * 4.0);

            voo.put("origem", origem);
            voo.put("destino", destino);
            voo.put("preco", precoBRL);
            voo.put("companhia", companhia);
            voo.put("data", dataPartida);
            voo.put("horario", horarioPartida + " - N/A");
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
     * Mapeia código de companhia
     */
    private String mapearCompanhia(String codigo) {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("la", "LATAM");
        mapping.put("g9", "Gol");
        mapping.put("ad", "Azul");
        mapping.put("aa", "American");
        mapping.put("ua", "United");
        mapping.put("ba", "British");
        
        for (String key : mapping.keySet()) {
            if (codigo.toLowerCase().contains(key)) {
                return mapping.get(key);
            }
        }
        
        return codigo.toUpperCase();
    }

    /**
     * Converte data de dd/MM/yyyy para yyyy-MM-dd
     */
    private String converterDataParaISO(String data) {
        if (data == null || data.isEmpty()) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
        try {
            String[] partes = data.split("/");
            return partes[2] + "-" + partes[1] + "-" + partes[0];
        } catch (Exception e) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
    }
}
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
public class KiwiService {

    // MUDE AQUI: Cole sua RapidAPI Key
    private static final String RAPIDAPI_KEY = "69e04958c4msh223796dab55e40ap10f77ajsn599fb6045336";
    private static final String RAPIDAPI_HOST = "kiwi-com-cheap-flights.p.rapidapi.com";
    private static final String BASE_URL = "https://kiwi-com-cheap-flights.p.rapidapi.com/round-trip";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    /**
     * Busca voos de ida e volta entre origem e destino
     */
    public List<Map<String, Object>> buscarVoos(String origem, String destino, String dataIda, String dataVolta) {
        List<Map<String, Object>> voos = new ArrayList<>();

        try {
            String dataIdaFormatada = converterData(dataIda);
            String dataVoltaFormatada = converterData(dataVolta);

            String url = String.format(
                "%s?source=%s&destination=%s&date_from=%s&date_to=%s&return_from=%s&return_to=%s&limit=10&sort=QUALITY",
                BASE_URL, origem, destino, dataIdaFormatada, dataIdaFormatada, dataVoltaFormatada, dataVoltaFormatada
            );

            System.out.println("🔍 Buscando: " + url);

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
                    
                    System.out.println("📝 Chaves JSON: " + jsonObject.keySet());

                    // Procurar por "data", "itineraries", ou "results"
                    JsonArray itineraries = null;
                    
                    if (jsonObject.has("data")) {
                        itineraries = jsonObject.getAsJsonArray("data");
                    } else if (jsonObject.has("itineraries")) {
                        itineraries = jsonObject.getAsJsonArray("itineraries");
                    } else if (jsonObject.has("results")) {
                        itineraries = jsonObject.getAsJsonArray("results");
                    }

                    if (itineraries != null && itineraries.size() > 0) {
                        System.out.println("✅ Encontrados " + itineraries.size() + " itinerários");

                        for (JsonElement element : itineraries) {
                            JsonObject itinerary = element.getAsJsonObject();
                            
                            try {
                                Map<String, Object> voo = extrairDadosVoo(itinerary, origem, destino, jsonObject);
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

            System.out.println("✅ Total de voos encontrados: " + voos.size());

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar voos: " + e.getMessage());
            e.printStackTrace();
        }

        return voos;
    }

    /**
     * Extrai dados de um voo/itinerário
     */
    private Map<String, Object> extrairDadosVoo(JsonObject itinerary, String origem, String destino, JsonObject jsonObject) {
        try {
            Map<String, Object> voo = new HashMap<>();

            String vooOrigem = origem;
            String vooDestino = destino;
            double preco = 0.0;
            String companhia = "Desconhecida";
            String dataPartida = "N/A";
            String horarioPartida = "N/A";
            int duracao = 0;
            int stopover = 0;

            // Extrair preço
            if (itinerary.has("price") && itinerary.get("price").isJsonObject()) {
                JsonObject priceObj = itinerary.getAsJsonObject("price");
                if (priceObj.has("amount")) {
                    preco = priceObj.get("amount").getAsDouble();
                }
            } else if (itinerary.has("price")) {
                preco = itinerary.get("price").getAsDouble();
            }

            // Converter USD para BRL (aproximadamente 5x)
            double precoBRL = preco > 0 ? preco * 5.0 : 350.0;

            // Extrair companhia
            if (itinerary.has("carriers") && itinerary.getAsJsonArray("carriers").size() > 0) {
                int carrierId = itinerary.getAsJsonArray("carriers").get(0).getAsInt();
                
                if (jsonObject.has("carriers")) {
                    JsonArray carriers = jsonObject.getAsJsonArray("carriers");
                    if (carrierId < carriers.size()) {
                        JsonObject carrier = carriers.get(carrierId).getAsJsonObject();
                        if (carrier.has("name")) {
                            companhia = carrier.get("name").getAsString();
                        }
                    }
                }
            }

            // Mapear companhia
            companhia = mapearCompanhia(companhia);

            // Extrair data/hora de partida
            if (itinerary.has("departure")) {
                String departure = itinerary.get("departure").getAsString();
                String[] partes = departure.split("T");
                if (partes.length > 0) {
                    dataPartida = partes[0].substring(8) + "/" + partes[0].substring(5, 7) + "/" + partes[0].substring(0, 4);
                }
                if (partes.length > 1) {
                    horarioPartida = partes[1].substring(0, 5);
                }
            }

            // Extrair duração
            if (itinerary.has("duration")) {
                duracao = itinerary.get("duration").getAsInt();
            }

            // Extrair número de escalas
            if (itinerary.has("stopover")) {
                stopover = itinerary.get("stopover").getAsInt();
            }

            int horas = duracao / 3600;
            int minutos = (duracao % 3600) / 60;
            String duracaoTexto = horas + "h " + minutos + "m";

            String tipo = stopover == 0 ? "Direto" : stopover + " Escala(s)";

            long milhas = (long) (precoBRL * 4.0);

            voo.put("origem", vooOrigem);
            voo.put("destino", vooDestino);
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
     * Mapeia nome de companhia para formato legível
     */
    private String mapearCompanhia(String nome) {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("LATAM", "LATAM");
        mapping.put("Gol", "Gol");
        mapping.put("Azul", "Azul");
        mapping.put("Air Arabia Maroc", "Air Arabia");
        mapping.put("Turkish Airlines", "Turkish");
        mapping.put("Lufthansa", "Lufthansa");
        
        for (String key : mapping.keySet()) {
            if (nome.contains(key)) {
                return mapping.get(key);
            }
        }
        
        return nome;
    }

    /**
     * Converte data de dd/MM/yyyy para ddMMyyyy
     */
    private String converterData(String data) {
        if (data == null || data.isEmpty()) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
        try {
            String[] partes = data.split("/");
            return partes[0] + partes[1] + partes[2];
        } catch (Exception e) {
            return LocalDate.now().plusDays(7).format(DATE_FORMATTER);
        }
    }
}
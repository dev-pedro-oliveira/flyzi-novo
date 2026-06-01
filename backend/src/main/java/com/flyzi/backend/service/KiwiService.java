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
            // Converter datas de dd/MM/yyyy para ddMMyyyy
            String dataIdaFormatada = converterData(dataIda);
            String dataVoltaFormatada = converterData(dataVolta);

            // Montar URL com parâmetros
            String url = String.format(
                "%s?source=%s&destination=%s&date_from=%s&date_to=%s&return_from=%s&return_to=%s&limit=30&sort=QUALITY&sort_order=ASCENDING",
                BASE_URL, origem, destino, dataIdaFormatada, dataIdaFormatada, dataVoltaFormatada, dataVoltaFormatada
            );

            System.out.println("🔍 Buscando voos: " + url);

            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            
            // Headers corretos para RapidAPI
            request.setHeader("x-rapidapi-key", RAPIDAPI_KEY);
            request.setHeader("x-rapidapi-host", RAPIDAPI_HOST);
            request.setHeader("Content-Type", "application/json");

            client.execute(request, response -> {
                System.out.println("📊 Status code: " + response.getCode());
                
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);

                System.out.println("📝 Response: " + json.substring(0, Math.min(500, json.length())));

                try {
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                    
                    // A resposta pode estar em "data" ou diretamente
                    JsonArray dataArray = jsonObject.has("data") 
                        ? jsonObject.getAsJsonArray("data") 
                        : jsonObject.getAsJsonArray("results");

                    if (dataArray != null) {
                        for (JsonElement element : dataArray) {
                            JsonObject flight = element.getAsJsonObject();
                            
                            try {
                                Map<String, Object> voo = extrairDadosVoo(flight, origem, destino);
                                if (voo != null) {
                                    voos.add(voo);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Erro ao processar voo: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao parsear JSON: " + e.getMessage());
                }

                return null;
            });

            System.out.println("✅ Carregados " + voos.size() + " voos de " + origem + " para " + destino);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar voos: " + e.getMessage());
            e.printStackTrace();
        }

        return voos;
    }

    /**
     * Extrai dados de um voo individual
     */
    private Map<String, Object> extrairDadosVoo(JsonObject flight, String origem, String destino) {
        Map<String, Object> voo = new HashMap<>();

        try {
            String vooOrigem = flight.has("flyFrom") ? flight.get("flyFrom").getAsString() : origem;
            String vooDestino = flight.has("flyTo") ? flight.get("flyTo").getAsString() : destino;
            double preco = flight.has("price") ? flight.get("price").getAsDouble() : 0.0;
            
            // Converter preço em USD para BRL (aproximadamente 5x)
            double precoBRL = preco * 5.0;
            
            // Companhia aérea
            String companhia = "Desconhecida";
            if (flight.has("airlines") && flight.getAsJsonArray("airlines").size() > 0) {
                companhia = mapearCompanhia(flight.getAsJsonArray("airlines").get(0).getAsString());
            }
            
            // Data e horário de partida
            String dataPartida = "N/A";
            String horarioPartida = "N/A";
            if (flight.has("utc_departure")) {
                try {
                    String departure = flight.get("utc_departure").getAsString();
                    String[] partes = departure.split("T");
                    dataPartida = partes[0].substring(8) + "/" + partes[0].substring(5, 7) + "/" + partes[0].substring(0, 4);
                    horarioPartida = partes.length > 1 ? partes[1].substring(0, 5) : "N/A";
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao processar data: " + e.getMessage());
                }
            }

            // Duração do voo
            int duracao = flight.has("duration") ? flight.get("duration").getAsJsonObject().get("total").getAsInt() : 0;
            int horas = duracao / 3600;
            int minutos = (duracao % 3600) / 60;
            String duracaoTexto = horas + "h " + minutos + "m";

            // Tipo de voo (direto ou com escalas)
            int stopover = flight.has("stopover_count") ? flight.get("stopover_count").getAsInt() : 0;
            String tipo = stopover == 0 ? "Direto" : stopover + " Escala(s)";

            // Milhas (basicamente preço * 1.5)
            long milhas = (long) (precoBRL * 1.5);

            voo.put("origem", vooOrigem);
            voo.put("destino", vooDestino);
            voo.put("preco", precoBRL);
            voo.put("companhia", companhia);
            voo.put("data", dataPartida);
            voo.put("horario", horarioPartida + " - N/A");
            voo.put("duracaoTexto", duracaoTexto);
            voo.put("duracaoMinutos", duracao / 60);
            voo.put("tipo", tipo);
            voo.put("milhasNum", milhas);
            voo.put("milhasFormatado", milhas > 0 ? String.format("%,d", milhas).replace(",", ".") : "0");
            voo.put("teveQueda", false);
            voo.put("continente", "N/A");
            voo.put("categoria", "Ida e volta");

            return voo;
        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair dados do voo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mapeia código de companhia para nome legível
     */
    private String mapearCompanhia(String codigoCompanhia) {
        Map<String, String> companhias = new HashMap<>();
        companhias.put("BA", "British Airways");
        companhias.put("AA", "American Airlines");
        companhias.put("DL", "Delta");
        companhias.put("UA", "United Airlines");
        companhias.put("LH", "Lufthansa");
        companhias.put("AF", "Air France");
        companhias.put("IB", "Iberia");
        companhias.put("KL", "KLM");
        companhias.put("SQ", "Singapore Airlines");
        companhias.put("JL", "Japan Airlines");
        companhias.put("CX", "Cathay Pacific");
        companhias.put("EK", "Emirates");
        companhias.put("QF", "Qantas");
        companhias.put("SN", "Brussels Airlines");
        companhias.put("OS", "Austrian Airlines");
        
        return companhias.getOrDefault(codigoCompanhia, codigoCompanhia);
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
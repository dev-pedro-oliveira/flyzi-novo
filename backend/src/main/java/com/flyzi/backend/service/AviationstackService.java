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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AviationstackService {

    // MUDE AQUI: Cole sua API Key do Aviationstack
    private static final String API_KEY = "a6a83999c3c23852ece3fb2676405932";
    private static final String BASE_URL = "https://api.aviationstack.com/v1/airports";

    /**
     * Busca aeroportos por país
     */
    public List<Map<String, String>> buscarAeroportosPorPais(String pais) {
        List<Map<String, String>> aeroportos = new ArrayList<>();

        try {
            String url = BASE_URL + "?access_key=" + API_KEY + "&search=" + pais + "&limit=100";
            
            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);

            client.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);
                
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonArray dataArray = jsonObject.getAsJsonArray("data");

                if (dataArray != null) {
                    for (JsonElement element : dataArray) {
                        JsonObject airport = element.getAsJsonObject();
                        Map<String, String> aero = new HashMap<>();
                        
                        aero.put("iataCode", airport.has("iata_code") && !airport.get("iata_code").isJsonNull() 
                            ? airport.get("iata_code").getAsString() 
                            : "");
                        aero.put("airportName", airport.has("airport_name") && !airport.get("airport_name").isJsonNull() 
                            ? airport.get("airport_name").getAsString() 
                            : "");
                        aero.put("city", airport.has("city_iata_code") && !airport.get("city_iata_code").isJsonNull() 
                            ? airport.get("city_iata_code").getAsString() 
                            : "");
                        aero.put("country", airport.has("country_iso2") && !airport.get("country_iso2").isJsonNull() 
                            ? airport.get("country_iso2").getAsString() 
                            : "");
                        
                        if (!aero.get("iataCode").isEmpty()) {
                            aeroportos.add(aero);
                        }
                    }
                }
                
                return null;
            });

            System.out.println("✅ Carregados " + aeroportos.size() + " aeroportos de " + pais);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar aeroportos de " + pais + ": " + e.getMessage());
            e.printStackTrace();
        }

        return aeroportos;
    }

    /**
     * Busca aeroportos de vários países
     */
    public List<Map<String, String>> buscarAeroportosMundiais() {
        List<Map<String, String>> todosAeroportos = new ArrayList<>();
        
        String[] paises = {
            "Brazil", "Portugal", "France", "United States", "Japan", 
            "Argentina", "Spain", "Netherlands", "Germany", "Italy", 
            "Mexico", "Canada", "Australia", "India", "China", 
            "United Kingdom", "Switzerland", "Thailand", "South Korea", "Singapore"
        };

        for (String pais : paises) {
            List<Map<String, String>> aerosPais = buscarAeroportosPorPais(pais);
            todosAeroportos.addAll(aerosPais);
            
            try {
                Thread.sleep(500); // Evitar rate limit
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("✅ Total de aeroportos mundiais carregados: " + todosAeroportos.size());
        return todosAeroportos;
    }
}
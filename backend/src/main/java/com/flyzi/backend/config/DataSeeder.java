package com.flyzi.backend.config;

import com.flyzi.backend.model.Voo;
import com.flyzi.backend.repository.VooRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private VooRepository vooRepository;

    @Override
    public void run(String... args) throws Exception {
        seedVoos();
    }

    private void seedVoos() {
        String[] companhias = {"Azul", "Gol", "Latam"};
        String[] origem = {"GRU", "VCP", "GIG", "BSB", "REC"};
        String[] destino = {"LIS", "CDG", "MIA", "NRT", "BRC"};
        String[] continentes = {"América do Sul", "América do Norte", "Europa", "Ásia"};
        
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy");
        DateTimeFormatter formatterISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dataBase = LocalDateTime.now().plusDays(5);

        int contador = 0;
        for (String orig : origem) {
            for (String dest : destino) {
                for (int i = 0; i < 5; i++) {
                    Voo voo = new Voo();
                    
                    voo.setOrigem(orig);
                    voo.setDestino(dest);
                    voo.setDescricaoRota(orig + " para " + dest);
                    
                    LocalDateTime data = dataBase.plusDays(i);
                    voo.setData(data.format(formatter));
                    voo.setDataISO(data.format(formatterISO));
                    
                    int horaPartida = 6 + random.nextInt(16);
                    int minPartida = random.nextBoolean() ? 0 : 30;
                    int duracao = 120 + random.nextInt(240);
                    int horaChegada = (horaPartida + duracao / 60) % 24;
                    int minChegada = minPartida + (duracao % 60);
                    if (minChegada >= 60) {
                        minChegada -= 60;
                        horaChegada++;
                    }
                    
                    voo.setHorario(String.format("%02d:%02d - %02d:%02d", horaPartida, minPartida, horaChegada, minChegada));
                    voo.setDuracaoMinutos(duracao);
                    int horas = duracao / 60;
                    int minutos = duracao % 60;
                    voo.setDuracaoTexto(String.format("%dh %02dm", horas, minutos));
                    
                    boolean direto = random.nextDouble() > 0.4;
                    voo.setTipo(direto ? "Direto" : "1 Escala");
                    
                    String companhia = companhias[random.nextInt(companhias.length)];
                    voo.setCompanhia(companhia);
                    voo.setClasseCor(companhia.equals("Azul") ? "tag-azul" : (companhia.equals("Latam") ? "tag-latam" : "tag-gol"));
                    
                    double precoBase = 300 + random.nextDouble() * 1200;
                    double variacaoPreco = (random.nextDouble() - 0.5) * 200;
                    double preco = precoBase + variacaoPreco;
                    voo.setPreco(preco);
                    
                    voo.setTeveQueda(random.nextDouble() < 0.2);
                    
                    int milhas = (int) (preco * 18) + random.nextInt(2000);
                    voo.setMilhasNum(milhas);
                    voo.setMilhasFormatado(String.valueOf(milhas).replaceAll("(\\d)(?=(\\d{3})+$)", "$1."));
                    
                    voo.setContinente(continentes[random.nextInt(continentes.length)]);
                    voo.setCategoria("Geral");
                    
                    vooRepository.save(voo);
                    contador++;
                }
            }
        }
        
        System.out.println("✅ " + contador + " voos criados com sucesso!");
    }
}

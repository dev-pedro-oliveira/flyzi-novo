package com.flyzi.backend.config;

import com.flyzi.backend.model.Aeroporto;
import com.flyzi.backend.model.Voo;
import com.flyzi.backend.repository.AeroportoRepository;
import com.flyzi.backend.repository.VooRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AeroportoRepository aeroportoRepository;

    @Autowired
    private VooRepository vooRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("🇧🇷 CARREGANDO DADOS DO FLYZI");
        System.out.println("========================================\n");

        vooRepository.deleteAll();
        aeroportoRepository.deleteAll();
        System.out.println("🗑️  Base de dados limpa\n");

        carregarAeroportosBrasileiros();
        criarVoosRealistas();

        System.out.println("\n========================================");
        System.out.println("✅ CARREGAMENTO COMPLETO!");
        System.out.println("========================================\n");
    }

    private void carregarAeroportosBrasileiros() {
        Aeroporto[] aeroportos = {
            new Aeroporto("GRU", "Aeroporto Internacional de São Paulo (Guarulhos)", "São Paulo", "SP"),
            new Aeroporto("CGH", "Aeroporto de Congonhas", "São Paulo", "SP"),
            new Aeroporto("GIG", "Aeroporto Internacional do Rio de Janeiro (Galeão)", "Rio de Janeiro", "RJ"),
            new Aeroporto("SDU", "Aeroporto Santos Dumont", "Rio de Janeiro", "RJ"),
            new Aeroporto("BSB", "Aeroporto Internacional de Brasília", "Brasília", "DF"),
            new Aeroporto("SSA", "Aeroporto Internacional de Salvador", "Salvador", "BA"),
            new Aeroporto("REC", "Aeroporto Internacional do Recife", "Recife", "PE"),
            new Aeroporto("BEL", "Aeroporto Internacional de Belém", "Belém", "PA"),
            new Aeroporto("FOR", "Aeroporto Internacional de Fortaleza", "Fortaleza", "CE"),
            new Aeroporto("CWB", "Aeroporto Internacional de Curitiba", "Curitiba", "PR"),
            new Aeroporto("POA", "Aeroporto Internacional de Porto Alegre", "Porto Alegre", "RS"),
            new Aeroporto("MAO", "Aeroporto Internacional de Manaus", "Manaus", "AM"),
            new Aeroporto("MCZ", "Aeroporto Internacional de Maceió", "Maceió", "AL"),
            new Aeroporto("JPA", "Aeroporto Internacional de João Pessoa", "João Pessoa", "PB"),
            new Aeroporto("VCP", "Aeroporto de Viracopos", "Campinas", "SP")
        };

        int totalAeroportos = 0;
        for (Aeroporto aero : aeroportos) {
            try {
                aeroportoRepository.save(aero);
                totalAeroportos++;
                System.out.println("✅ " + aero.getIata() + " - " + aero.getNome());
            } catch (Exception e) {
                System.err.println("⚠️  Erro ao salvar " + aero.getIata() + ": " + e.getMessage());
            }
        }

        System.out.println("\n📊 Total de aeroportos carregados: " + totalAeroportos + "\n");
    }

    private void criarVoosRealistas() {
        System.out.println("🔍 CRIANDO VOOS REALISTAS");
        
        LocalDate dataIda = LocalDate.now().plusDays(10);
        String dataIdaStr = dataIda.format(FORMATTER);

        Object[][] voos = {
            // GRU → GIG (São Paulo → Rio)
            {"GRU", "GIG", 350.0, "LATAM", "09:00 - 11:30", "2h 30m", 150, "Direto", 525, "Brasil"},
            {"GRU", "GIG", 420.0, "Gol", "11:00 - 13:15", "2h 15m", 135, "Direto", 630, "Brasil"},
            {"GRU", "GIG", 380.0, "Azul", "14:00 - 16:45", "2h 45m", 165, "Direto", 570, "Brasil"},
            
            // GRU → SSA (São Paulo → Salvador)
            {"GRU", "SSA", 280.0, "LATAM", "08:30 - 11:45", "3h 15m", 195, "Direto", 420, "Brasil"},
            {"GRU", "SSA", 310.0, "Gol", "10:00 - 13:30", "3h 30m", 210, "Direto", 465, "Brasil"},
            {"GRU", "SSA", 295.0, "Azul", "15:45 - 19:00", "3h 15m", 195, "Direto", 442, "Brasil"},
            
            // GRU → REC (São Paulo → Recife)
            {"GRU", "REC", 290.0, "LATAM", "07:00 - 10:15", "3h 15m", 195, "Direto", 435, "Brasil"},
            {"GRU", "REC", 320.0, "Gol", "09:30 - 12:45", "3h 15m", 195, "Direto", 480, "Brasil"},
            {"GRU", "REC", 305.0, "Azul", "16:00 - 19:15", "3h 15m", 195, "Direto", 457, "Brasil"},
            
            // GIG → SSA (Rio → Salvador)
            {"GIG", "SSA", 250.0, "LATAM", "10:00 - 12:45", "2h 45m", 165, "Direto", 375, "Brasil"},
            {"GIG", "SSA", 275.0, "Gol", "13:00 - 15:45", "2h 45m", 165, "Direto", 412, "Brasil"},
            
            // GIG → BSB (Rio → Brasília)
            {"GIG", "BSB", 320.0, "LATAM", "08:00 - 09:45", "1h 45m", 105, "Direto", 480, "Brasil"},
            {"GIG", "BSB", 340.0, "Gol", "11:00 - 12:45", "1h 45m", 105, "Direto", 510, "Brasil"},
            
            // BSB → CWB (Brasília → Curitiba)
            {"BSB", "CWB", 380.0, "LATAM", "09:30 - 11:45", "2h 15m", 135, "Direto", 570, "Brasil"},
            {"BSB", "CWB", 395.0, "Gol", "14:00 - 16:15", "2h 15m", 135, "Direto", 592, "Brasil"},
            
            // CWB → POA (Curitiba → Porto Alegre)
            {"CWB", "POA", 280.0, "LATAM", "10:00 - 11:45", "1h 45m", 105, "Direto", 420, "Brasil"},
            {"CWB", "POA", 295.0, "Gol", "15:30 - 17:15", "1h 45m", 105, "Direto", 442, "Brasil"},
            
            // GRU → BSB (São Paulo → Brasília)
            {"GRU", "BSB", 320.0, "LATAM", "08:00 - 09:45", "1h 45m", 105, "Direto", 480, "Brasil"},
            {"GRU", "BSB", 340.0, "Gol", "12:30 - 14:15", "1h 45m", 105, "Direto", 510, "Brasil"},
            
            // GRU → FOR (São Paulo → Fortaleza)
            {"GRU", "FOR", 310.0, "LATAM", "07:00 - 10:30", "3h 30m", 210, "Direto", 465, "Brasil"},
            {"GRU", "FOR", 340.0, "Gol", "09:00 - 12:30", "3h 30m", 210, "Direto", 510, "Brasil"},
            
            // GRU → BEL (São Paulo → Belém)
            {"GRU", "BEL", 350.0, "LATAM", "08:30 - 12:00", "3h 30m", 210, "Direto", 525, "Brasil"},
            {"GRU", "BEL", 375.0, "Gol", "10:00 - 13:30", "3h 30m", 210, "Direto", 562, "Brasil"},
            
            // SSA → REC (Salvador → Recife)
            {"SSA", "REC", 180.0, "LATAM", "11:00 - 12:30", "1h 30m", 90, "Direto", 270, "Brasil"},
            {"SSA", "REC", 195.0, "Gol", "14:00 - 15:30", "1h 30m", 90, "Direto", 292, "Brasil"},
        };

        int totalVoos = 0;
        for (Object[] vooData : voos) {
            try {
                Voo voo = new Voo();
                
                voo.setOrigem((String) vooData[0]);
                voo.setDestino((String) vooData[1]);
                voo.setPreco((Double) vooData[2]);
                voo.setCompanhia((String) vooData[3]);
                voo.setHorario((String) vooData[4]);
                voo.setDuracaoTexto((String) vooData[5]);
                voo.setDuracaoMinutos((Integer) vooData[6]);
                voo.setTipo((String) vooData[7]);
                voo.setMilhasNum((Integer) vooData[8]);
                voo.setContinente((String) vooData[9]);
                
                voo.setDescricaoRota(voo.getOrigem() + " → " + voo.getDestino());
                voo.setData(dataIdaStr);
                voo.setDataISO(LocalDate.now().toString());
                voo.setClasseCor("blue");
                voo.setMilhasFormatado(String.format("%,d", voo.getMilhasNum()).replace(",", "."));
                voo.setTeveQueda(Math.random() < 0.1); // 10% chance de preço caiu
                voo.setCategoria("Doméstico");

                vooRepository.save(voo);
                totalVoos++;

            } catch (Exception e) {
                System.err.println("⚠️  Erro ao salvar voo: " + e.getMessage());
            }
        }

        System.out.println("📊 Total de voos carregados: " + totalVoos + "\n");
    }
}
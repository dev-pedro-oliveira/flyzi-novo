package com.flyzi.backend.config;

import com.flyzi.backend.model.Aeroporto;
import com.flyzi.backend.model.Voo;
import com.flyzi.backend.repository.AeroportoRepository;
import com.flyzi.backend.repository.VooRepository;
import com.flyzi.backend.service.KiwiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AeroportoRepository aeroportoRepository;

    @Autowired
    private VooRepository vooRepository;

    @Autowired
    private KiwiService kiwiService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("🇧🇷 CARREGANDO AEROPORTOS BRASILEIROS");
        System.out.println("========================================\n");

        // Limpar dados antigos
        vooRepository.deleteAll();
        aeroportoRepository.deleteAll();
        System.out.println("🗑️  Base de dados limpa\n");

        // Carregar aeroportos brasileiros maiores
        carregarAeroportosBrasileiros();

        // Buscar voos entre as rotas domésticas
        buscarVoosDoesticos();

        System.out.println("\n========================================");
        System.out.println("✅ CARREGAMENTO COMPLETO!");
        System.out.println("========================================\n");
    }

    /**
     * Carrega os 15 maiores aeroportos brasileiros
     */
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

    /**
     * Busca voos entre as principais rotas domésticas
     */
    private void buscarVoosDoesticos() {
        // Principais rotas domésticas
        String[][] rotas = {
            {"GRU", "GIG"},
            {"GRU", "SSA"},
            {"GRU", "REC"},
            {"GRU", "BEL"},
            {"GRU", "FOR"},
            {"GRU", "CWB"},
            {"GRU", "POA"},
            {"GRU", "MAO"},
            {"GIG", "SSA"},
            {"GIG", "BSB"},
            {"GIG", "REC"},
            {"SSA", "REC"},
            {"BSB", "CWB"},
            {"CWB", "POA"},
            {"GRU", "BSB"}
        };

        LocalDate dataIda = LocalDate.now().plusDays(10);
        LocalDate dataVolta = LocalDate.now().plusDays(17);
        
        String dataIdaFormatada = dataIda.format(FORMATTER);
        String dataVoltaFormatada = dataVolta.format(FORMATTER);

        System.out.println("🔍 BUSCANDO VOOS DOMÉSTICOS");
        System.out.println("📅 Data de Ida: " + dataIdaFormatada);
        System.out.println("📅 Data de Volta: " + dataVoltaFormatada);
        System.out.println("🚀 Buscando em " + rotas.length + " rotas...\n");

        int totalVoos = 0;

        for (String[] rota : rotas) {
            String origem = rota[0];
            String destino = rota[1];

            System.out.println("🔄 Buscando: " + origem + " → " + destino);

            try {
                List<Map<String, Object>> voos = kiwiService.buscarVoos(origem, destino, dataIdaFormatada, dataVoltaFormatada);

                if (voos != null && !voos.isEmpty()) {
                    for (Map<String, Object> vooData : voos) {
                        try {
                            Voo voo = new Voo();
                            
                            voo.setOrigem(getStringValue(vooData, "origem", origem));
                            voo.setDestino(getStringValue(vooData, "destino", destino));
                            voo.setDescricaoRota(voo.getOrigem() + " → " + voo.getDestino());
                            voo.setData(getStringValue(vooData, "data", dataIdaFormatada));
                            voo.setDataISO(LocalDate.now().toString());
                            voo.setHorario(getStringValue(vooData, "horario", "10:00 - 18:00"));
                            voo.setDuracaoTexto(getStringValue(vooData, "duracaoTexto", "2h 30m"));
                            voo.setDuracaoMinutos(getIntValue(vooData, "duracaoMinutos", 150));
                            voo.setTipo(getStringValue(vooData, "tipo", "Direto"));
                            voo.setPreco(getDoubleValue(vooData, "preco", 500.0));
                            voo.setCompanhia(getStringValue(vooData, "companhia", "LATAM"));
                            voo.setClasseCor("blue");
                            voo.setMilhasNum(getIntValue(vooData, "milhasNum", 1000));
                            voo.setMilhasFormatado(getStringValue(vooData, "milhasFormatado", "1.000"));
                            voo.setTeveQueda(false);
                            voo.setContinente("América do Sul");
                            voo.setCategoria("Doméstico");

                            vooRepository.save(voo);
                            totalVoos++;

                        } catch (Exception e) {
                            System.err.println("   ⚠️  Erro ao salvar voo: " + e.getMessage());
                        }
                    }
                    System.out.println("   ✅ " + voos.size() + " voos carregados\n");

                } else {
                    System.out.println("   ⚠️  Nenhum voo encontrado\n");
                }

            } catch (Exception e) {
                System.err.println("   ❌ Erro: " + e.getMessage() + "\n");
            }

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("📊 Total de voos carregados: " + totalVoos);
    }

    // ============================================================
    // FUNÇÕES AUXILIARES
    // ============================================================

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
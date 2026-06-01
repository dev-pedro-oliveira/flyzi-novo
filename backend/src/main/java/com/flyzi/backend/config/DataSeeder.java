package com.flyzi.backend.config;

import com.flyzi.backend.model.Aeroporto;
import com.flyzi.backend.model.Voo;
import com.flyzi.backend.repository.AeroportoRepository;
import com.flyzi.backend.repository.VooRepository;
import com.flyzi.backend.service.GoogleFlightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AeroportoRepository aeroportoRepository;

    @Autowired
    private VooRepository vooRepository;

    @Autowired
    private GoogleFlightsService googleFlightsService;

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
        buscarVoosReaisMultiplasDatas();

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

        System.out.println("\n📊 Total de aeroportos: " + totalAeroportos + "\n");
    }

    private void buscarVoosReaisMultiplasDatas() {
        System.out.println("🔍 BUSCANDO VOOS COM MÚLTIPLAS DATAS (SEM LIMITE)\n");
        
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

        int totalVoos = 0;
        int voosComSucesso = 0;
        int voosComFallback = 0;
        Set<String> voosDuplicados = new HashSet<>();

        System.out.println("📅 Buscando voos para os próximos 7 dias");
        System.out.println("🚀 Buscando em " + rotas.length + " rotas com múltiplas datas...\n");

        // Para cada rota
        for (String[] rota : rotas) {
            String origem = rota[0];
            String destino = rota[1];

            System.out.println("🔄 ROTA: " + origem + " → " + destino);

            int voosRota = 0;

            // Buscar para os próximos 5 dias (5, 6, 7, 8, 9 dias no futuro)
            for (int diasAdelante = 5; diasAdelante <= 9; diasAdelante++) {
                LocalDate dataIda = LocalDate.now().plusDays(diasAdelante);
                LocalDate dataVolta = dataIda.plusDays(7); // 7 dias de volta
                
                String dataIdaStr = dataIda.format(FORMATTER);
                String dataVoltaStr = dataVolta.format(FORMATTER);

                System.out.println("   📅 Tentando: " + dataIdaStr + " → " + dataVoltaStr);

                try {
                    // Buscar dados reais
                    List<Map<String, Object>> voos = googleFlightsService.buscarVoos(
                        origem, destino, dataIdaStr, dataVoltaStr
                    );

                    if (voos != null && !voos.isEmpty()) {
                        for (Map<String, Object> vooData : voos) {
                            try {
                                // Criar chave única para evitar duplicatas
                                String chaveVoo = origem + "-" + destino + "-" + 
                                    getStringValue(vooData, "data", "") + "-" +
                                    getStringValue(vooData, "horario", "") + "-" +
                                    getDoubleValue(vooData, "preco", 0);

                                if (!voosDuplicados.contains(chaveVoo)) {
                                    Voo voo = new Voo();
                                    
                                    voo.setOrigem(getStringValue(vooData, "origem", origem));
                                    voo.setDestino(getStringValue(vooData, "destino", destino));
                                    voo.setDescricaoRota(voo.getOrigem() + " → " + voo.getDestino());
                                    voo.setData(getStringValue(vooData, "data", dataIdaStr));
                                    voo.setDataISO(LocalDate.now().toString());
                                    voo.setHorario(getStringValue(vooData, "horario", "10:00 - 18:00"));
                                    voo.setDuracaoTexto(getStringValue(vooData, "duracaoTexto", "2h 30m"));
                                    voo.setDuracaoMinutos(getIntValue(vooData, "duracaoMinutos", 150));
                                    voo.setTipo(getStringValue(vooData, "tipo", "Direto"));
                                    voo.setPreco(getDoubleValue(vooData, "preco", 350.0));
                                    voo.setCompanhia(getStringValue(vooData, "companhia", "LATAM"));
                                    voo.setClasseCor("blue");
                                    voo.setMilhasNum(getIntValue(vooData, "milhasNum", 1500));
                                    voo.setMilhasFormatado(getStringValue(vooData, "milhasFormatado", "1.500"));
                                    voo.setTeveQueda(Math.random() < 0.15);
                                    voo.setContinente("América do Sul");
                                    voo.setCategoria("Doméstico");

                                    vooRepository.save(voo);
                                    voosDuplicados.add(chaveVoo);
                                    totalVoos++;
                                    voosRota++;
                                    voosComSucesso++;
                                }

                            } catch (Exception e) {
                                System.err.println("      ⚠️  Erro ao salvar voo: " + e.getMessage());
                            }
                        }
                        System.out.println("      ✅ " + voos.size() + " voos encontrados\n");

                    } else {
                        System.out.println("      ⚠️  Nenhum voo encontrado\n");
                    }

                } catch (Exception e) {
                    System.err.println("      ❌ Erro: " + e.getMessage() + "\n");
                }

                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Se não encontrou nada em nenhuma data, criar fallback
            if (voosRota == 0) {
                System.out.println("   💾 Nenhum voo real encontrado. Criando fallback...");
                for (int i = 0; i < 3; i++) {
                    LocalDate data = LocalDate.now().plusDays(5 + i);
                    criarVooFallback(origem, destino, data.format(FORMATTER));
                    totalVoos++;
                    voosComFallback++;
                }
                System.out.println("   ✅ 3 voos fallback criados\n");
            }
        }

        System.out.println("📊 RESUMO FINAL:");
        System.out.println("   ✅ Voos REAIS do Google Flights: " + voosComSucesso);
        System.out.println("   ⚠️  Voos FALLBACK (offline): " + voosComFallback);
        System.out.println("   📊 Total de voos carregados: " + totalVoos + "\n");
    }

    private void criarVooFallback(String origem, String destino, String data) {
        try {
            Voo voo = new Voo();
            
            voo.setOrigem(origem);
            voo.setDestino(destino);
            voo.setDescricaoRota(origem + " → " + destino);
            voo.setData(data);
            voo.setDataISO(LocalDate.now().toString());
            voo.setHorario("10:00 - 13:00");
            voo.setDuracaoTexto("3h 0m");
            voo.setDuracaoMinutos(180);
            voo.setTipo("Direto");
            voo.setPreco(350.0 + (Math.random() * 200));
            voo.setCompanhia(new String[]{"LATAM", "Gol", "Azul"}[(int)(Math.random() * 3)]);
            voo.setClasseCor("blue");
            
            long milhas = (long) (voo.getPreco() * 4.0 + (Math.random() * 1000));
            voo.setMilhasNum((int) milhas);
            voo.setMilhasFormatado(String.format("%,d", milhas).replace(",", "."));
            
            voo.setTeveQueda(Math.random() < 0.15);
            voo.setContinente("América do Sul");
            voo.setCategoria("Doméstico");

            vooRepository.save(voo);

        } catch (Exception e) {
            System.err.println("      ❌ Erro ao criar fallback: " + e.getMessage());
        }
    }

    // ============================================================
    // FUNÇÕES AUXILIARES
    // ============================================================

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map == null) return defaultValue;
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
        if (map == null) return defaultValue;
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
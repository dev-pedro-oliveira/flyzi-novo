package com.flyzi.backend.controller;

import com.flyzi.backend.model.Aeroporto;
import com.flyzi.backend.repository.AeroportoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aeroportos")
@CrossOrigin(origins = "*")
public class AeroportoController {

    @Autowired
    private AeroportoRepository aeroportoRepository;

    /**
     * GET /api/aeroportos
     * Retorna todos os aeroportos
     */
    @GetMapping
    public ResponseEntity<List<Aeroporto>> listarAeroportos() {
        List<Aeroporto> aeroportos = aeroportoRepository.findAll();
        System.out.println("✅ GET /api/aeroportos - Total: " + aeroportos.size());
        return ResponseEntity.ok(aeroportos);
    }

    /**
     * GET /api/aeroportos/{iata}
     * Retorna um aeroporto específico
     */
    @GetMapping("/{iata}")
    public ResponseEntity<Aeroporto> obterAeroporto(@PathVariable String iata) {
        return aeroportoRepository.findByIata(iata.toUpperCase())
                .map(aero -> {
                    System.out.println("✅ GET /api/aeroportos/" + iata);
                    return ResponseEntity.ok(aero);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/aeroportos/estado/{estado}
     * Retorna aeroportos de um estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Aeroporto>> obterPorEstado(@PathVariable String estado) {
        List<Aeroporto> aeroportos = aeroportoRepository.findByEstado(estado.toUpperCase());
        System.out.println("✅ GET /api/aeroportos/estado/" + estado + " - Total: " + aeroportos.size());
        return ResponseEntity.ok(aeroportos);
    }
}
package com.flyzi.backend.controller;

import com.flyzi.backend.model.Voo;
import com.flyzi.backend.repository.VooRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voos")
@CrossOrigin(origins = "*")
public class VooController {

    @Autowired
    private VooRepository vooRepository;

    /**
     * GET /api/voos
     * Retorna todos os voos cadastrados
     */
    @GetMapping
    public List<Voo> listarVoos() {
        System.out.println("✅ GET /api/voos - Retornando voos");
        List<Voo> voos = vooRepository.findAll();
        System.out.println("   Total: " + voos.size() + " voos");
        return voos;
    }

    /**
     * POST /api/voos
     * Cria um novo voo
     */
    @PostMapping
    public ResponseEntity<Voo> criarVoo(@RequestBody Voo voo) {
        System.out.println("✅ POST /api/voos - Criando novo voo");
        Voo salvo = vooRepository.save(voo);
        return ResponseEntity.status(201).body(salvo);
    }

    /**
     * DELETE /api/voos/{id}
     * Deleta um voo pelo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVoo(@PathVariable Long id) {
        System.out.println("✅ DELETE /api/voos/" + id);
        if (!vooRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vooRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
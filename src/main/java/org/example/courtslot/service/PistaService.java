package org.example.courtslot.service;

import org.example.courtslot.dao.PistaDAO;
import org.example.courtslot.model.Pista;

import java.util.List;
import java.util.Optional;

public class PistaService {

    private final PistaDAO pistaDAO;

    public PistaService() {
        this.pistaDAO = new PistaDAO();
    }

    public PistaService(PistaDAO pistaDAO) {
        this.pistaDAO = pistaDAO;
    }

    public void save(Pista pista) {
        validar(pista);
        pistaDAO.save(pista);
    }

    public void update(Pista pista) {
        validar(pista);
        pistaDAO.update(pista);
    }

    public void delete(Long id) {
        pistaDAO.delete(id);
    }

    public List<Pista> getAll() {
        return pistaDAO.getAll();
    }

    public List<Pista> getAllIncludingDesactivadas() {
        return pistaDAO.getAllIncludingDesactivadas();
    }

    public Optional<Pista> findById(Long id) {
        return pistaDAO.findById(id);
    }

    public List<Pista> getByDeporte(Long deporteId) {
        return pistaDAO.getByDeporte(deporteId);
    }

    public int contarPorDeporte(Long deporteId) {
        return pistaDAO.contarPorDeporte(deporteId);
    }

    public Pista getPistaMasBarata(Long deporteId) {
        List<Pista> pistas = pistaDAO.getByDeporte(deporteId);
        Pista pistaMasBarata = null;
        for (Pista p : pistas) {
            if (pistaMasBarata == null || p.getPrecioPorHora() < pistaMasBarata.getPrecioPorHora()) {
                pistaMasBarata = p;
            }
        }
        return pistaMasBarata;
    }

    private void validar(Pista pista) {
        if (pista.getNombre() == null || pista.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la pista no puede estar vacío.");
        if (pista.getPrecioPorHora() == null || pista.getPrecioPorHora() <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        if (pista.getDeporte() == null)
            throw new IllegalArgumentException("Debe asignarse un deporte a la pista.");
    }
}
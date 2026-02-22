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

    private void validar(Pista pista) {
        if (pista.getNombre() == null || pista.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la pista no puede estar vacío.");
        if (pista.getPrecioPorHora() == null || pista.getPrecioPorHora() <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        if (pista.getDeporte() == null)
            throw new IllegalArgumentException("Debe asignarse un deporte a la pista.");
    }
}

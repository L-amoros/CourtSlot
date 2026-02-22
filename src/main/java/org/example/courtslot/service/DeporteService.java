package org.example.courtslot.service;

import org.example.courtslot.dao.DeporteDAO;
import org.example.courtslot.model.Deporte;

import java.util.List;

public class DeporteService {

    private final DeporteDAO deporteDAO;

    public DeporteService() {
        this.deporteDAO = new DeporteDAO();
    }

    public DeporteService(DeporteDAO deporteDAO) {
        this.deporteDAO = deporteDAO;
    }

    public void save(Deporte deporte) {
        if (deporte.getNombre() == null || deporte.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del deporte no puede estar vacío.");
        if (deporteDAO.existeNombre(deporte.getNombre()))
            throw new IllegalArgumentException("Ya existe un deporte con ese nombre.");
        deporteDAO.save(deporte);
    }

    public void update(Deporte deporte) {
        if (deporte.getNombre() == null || deporte.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del deporte no puede estar vacío.");
        deporteDAO.update(deporte);
    }

    public void delete(Long id) {
        deporteDAO.delete(id);
    }

    public List<Deporte> getAll() {
        return deporteDAO.getAll();
    }
}

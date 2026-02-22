package org.example.courtslot.dao;

import org.example.courtslot.model.Reserva;
import org.example.courtslot.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ReservaDAO {

    public void save(Reserva reserva) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(reserva);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la reserva", e);
        }
    }

    public void cancelar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Reserva r = session.get(Reserva.class, id);
            if (r != null) {
                r.setEstado(Reserva.Estado.CANCELADA);
                session.merge(r);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Optional<Reserva> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Reserva.class, id));
        }
    }

    public List<Reserva> findByUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Reserva r WHERE r.usuario.id = :uid ORDER BY r.fecha, r.horaInicio",
                    Reserva.class)
                    .setParameter("uid", usuarioId)
                    .list();
        }
    }

    public List<Reserva> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Reserva r ORDER BY r.fecha DESC, r.horaInicio", Reserva.class).list();
        }
    }

    public boolean existeSolapamiento(Long pistaId, LocalDate fecha,
                                      LocalTime horaInicio, LocalTime horaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(r) FROM Reserva r " +
                    "WHERE r.pista.id = :pistaId AND r.fecha = :fecha " +
                    "AND r.estado <> 'CANCELADA' " +
                    "AND r.horaInicio < :horaFin AND r.horaFin > :horaInicio",
                    Long.class)
                    .setParameter("pistaId", pistaId)
                    .setParameter("fecha", fecha)
                    .setParameter("horaInicio", horaInicio)
                    .setParameter("horaFin", horaFin)
                    .getSingleResult();
            return count > 0;
        }
    }

    public List<Reserva> findByPistaYFecha(Long pistaId, LocalDate fecha) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Reserva r WHERE r.pista.id = :pistaId AND r.fecha = :fecha " +
                    "AND r.estado <> 'CANCELADA' ORDER BY r.horaInicio",
                    Reserva.class)
                    .setParameter("pistaId", pistaId)
                    .setParameter("fecha", fecha)
                    .list();
        }
    }
}

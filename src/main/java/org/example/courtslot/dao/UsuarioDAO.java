package org.example.courtslot.dao;

import org.example.courtslot.model.Usuario;
import org.example.courtslot.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;

public class UsuarioDAO {

    // ── Guardar nuevo usuario ─────────────────────────────────────────────────
    public void save(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(usuario);      // INSERT en BD
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // ── Buscar por email (usado en el login) ──────────────────────────────────
    // Usamos HQL parametrizado para evitar SQL Injection
    public Optional<Usuario> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        }
    }

    // ── Comprobar si ya existe un email (usado en el registro) ────────────────
    public boolean existeEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        }
    }
}

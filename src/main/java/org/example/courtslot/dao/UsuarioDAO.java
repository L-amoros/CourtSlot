package org.example.courtslot.dao;

import org.example.courtslot.model.Usuario;
import org.example.courtslot.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    public void save(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(usuario);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Optional<Usuario> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        }
    }

    public boolean existeEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        }
    }
    public List<Usuario> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Usuario", Usuario.class).list();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Usuario u = session.get(Usuario.class, id);
            if (u != null) session.remove(u);
            tx.commit();
        }
    }
}

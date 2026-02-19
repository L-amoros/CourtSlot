package org.example.courtslot.model;

import jakarta.persistence.*;
@Entity
@Table(name = "usuarios")
public class Usuario {
    public enum Rol {
        USER, ADMIN
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false, length = 255)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Rol rol;

    // ─── Constructores ────────────────────────────────────────────────────────

    // Constructor vacío — obligatorio para Hibernate
    public Usuario() {}

    // Constructor de uso en el código
    public Usuario(String nombre, String email, String password, Rol rol) {
        this.nombre   = nombre;
        this.email    = email;
        this.password = password;
        this.rol      = rol;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long getId()                 { return id; }
    public void setId(Long id)          { this.id = id; }

    public String getNombre()           { return nombre; }
    public void setNombre(String n)     { this.nombre = n; }

    public String getEmail()            { return email; }
    public void setEmail(String e)      { this.email = e; }

    public String getPassword()         { return password; }
    public void setPassword(String p)   { this.password = p; }

    public Rol getRol()                 { return rol; }
    public void setRol(Rol r)           { this.rol = r; }

    // Método de ayuda para comprobar si es admin
    public boolean isAdmin()            { return Rol.ADMIN.equals(this.rol); }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nombre='" + nombre + "', email='" + email + "', rol=" + rol + "}";
    }
}

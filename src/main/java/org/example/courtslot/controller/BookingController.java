package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.courtslot.model.Deporte;
import org.example.courtslot.model.Pista;
import org.example.courtslot.model.Reserva;
import org.example.courtslot.model.Usuario;
import org.example.courtslot.service.PistaService;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BookingController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Label  fechaLabel;      // "lun. 16 feb 2026"
    @FXML private Button btnAnterior;     // <
    @FXML private Button btnSiguiente;    // >
    @FXML private VBox   pistasContainer; // fila por cada pista
    @FXML private Label  tituloLabel;     // "Reserva de Pistas"

    // ── Estado ────────────────────────────────────────────────────────────────
    private Deporte    deporteSeleccionado;
    private LocalDate  fechaActual = LocalDate.now();

    private final PistaService   pistaService   = new PistaService();
    private final ReservaService reservaService = new ReservaService();

    private static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_CIERRE   = LocalTime.of(21, 0); // slots hasta 20:00-21:00
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actualizarFechaLabel();
    }

    // ── Inyección desde HomepageController ───────────────────────────────────
    /**
     * Recibe el deporte seleccionado y carga todas sus pistas.
     */
    public void setDeporte(Deporte deporte) {
        this.deporteSeleccionado = deporte;
        tituloLabel.setText("Reserva de Pistas · " + deporte.getNombre());
        cargarPistas();
    }

    /**
     * Compatibilidad: si se llama con una Pista directamente,
     * mostramos solo esa pista (deporte con una pista).
     */
    public void setPista(Pista pista) {
        this.deporteSeleccionado = pista.getDeporte();
        tituloLabel.setText("Reserva de Pistas · " + pista.getDeporte().getNombre());
        cargarPistasFiltrando(List.of(pista));
    }

    // ── Carga de pistas ───────────────────────────────────────────────────────
    private void cargarPistas() {
        List<Pista> pistas = pistaService.getAll().stream()
                .filter(p -> p.getDeporte().getId().equals(deporteSeleccionado.getId()))
                .collect(Collectors.toList());
        cargarPistasFiltrando(pistas);
    }

    private void cargarPistasFiltrando(List<Pista> pistas) {
        pistasContainer.getChildren().clear();

        if (pistas.isEmpty()) {
            Label empty = new Label("No hay pistas disponibles para este deporte.");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 14px;");
            pistasContainer.getChildren().add(empty);
            return;
        }

        for (Pista pista : pistas) {
            pistasContainer.getChildren().add(crearFilaPista(pista));
        }
    }

    // ── Construye la fila de una pista ────────────────────────────────────────
    private VBox crearFilaPista(Pista pista) {
        // Contenedor de la fila
        VBox fila = new VBox(10);
        fila.getStyleClass().add("pista-row");

        // Cabecera: nombre + descripción
        Label nombre = new Label(pista.getNombre());
        nombre.getStyleClass().add("pista-nombre");

        Label desc = new Label(
                (pista.getDescripcion() != null && !pista.getDescripcion().isBlank())
                        ? pista.getDescripcion()
                        : String.format("%.2f €/h", pista.getPrecioPorHora())
        );
        desc.getStyleClass().add("pista-desc");

        VBox cabecera = new VBox(2, nombre, desc);

        // Fila de slots horarios
        HBox slots = new HBox(6);
        slots.setAlignment(Pos.CENTER_LEFT);
        slots.getStyleClass().add("slots-row");

        // Obtener reservas bloqueadas para esta pista y fecha
        List<Reserva> bloqueados = reservaService.getSlotsBloqueados(pista.getId(), fechaActual);

        Long usuarioActualId = SessionManager.getInstance().estaLogueado()
                ? SessionManager.getInstance().getUsuarioActual().getId()
                : null;

        LocalTime hora = HORA_APERTURA;
        while (hora.isBefore(HORA_CIERRE)) {
            final LocalTime slotInicio = hora;
            final LocalTime slotFin    = hora.plusHours(1);

            // Buscar si hay una reserva que solapa este slot
            Reserva reservaSolapa = bloqueados.stream()
                    .filter(r -> r.getHoraInicio().isBefore(slotFin) && r.getHoraFin().isAfter(slotInicio))
                    .findFirst()
                    .orElse(null);

            Button btn = new Button();
            btn.getStyleClass().add("slot-btn");
            btn.setPrefWidth(80);
            btn.setPrefHeight(50);

            if (reservaSolapa != null) {
                boolean esMia = usuarioActualId != null
                        && reservaSolapa.getUsuario().getId().equals(usuarioActualId);

                if (esMia) {
                    // Naranja: reserva propia — se puede cancelar
                    btn.setText("⚑\nMi reserva");
                    btn.getStyleClass().add("slot-mio");
                    btn.setCursor(javafx.scene.Cursor.HAND);
                    final Reserva reservaACancelar = reservaSolapa;
                    btn.setOnAction(e -> cancelarMiReservaDesdeSlot(reservaACancelar, btn));
                } else {
                    // Rojo: ocupado por otro usuario
                    btn.setText("✓\nOcupado");
                    btn.getStyleClass().add("slot-ocupado");
                    btn.setMouseTransparent(true);
                }
            } else {
                // Verde: libre
                btn.setText(slotInicio.toString());
                btn.getStyleClass().add("slot-libre");
                btn.setCursor(javafx.scene.Cursor.HAND);
                btn.setOnAction(e -> confirmarReserva(pista, slotInicio, slotFin, btn, slots));
            }

            slots.getChildren().add(btn);
            hora = hora.plusHours(1);
        }

        fila.getChildren().addAll(cabecera, slots);
        return fila;
    }

    // ── Cancelar reserva propia desde un slot naranja ─────────────────────────
    private void cancelarMiReservaDesdeSlot(Reserva reserva, Button btn) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Cancelar tu reserva de %s\nel %s de %s a %s?",
                        reserva.getPista().getNombre(),
                        fechaActual.format(DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"))),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin()),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                reservaService.cancelar(reserva.getId());

                // Actualizar visualmente el slot a libre
                btn.setText(reserva.getHoraInicio().toString());
                btn.getStyleClass().remove("slot-mio");
                btn.getStyleClass().add("slot-libre");
                btn.setCursor(javafx.scene.Cursor.HAND);
                btn.setMouseTransparent(false);
                // Reasignar acción de reservar
                btn.setOnAction(e -> confirmarReserva(
                        reserva.getPista(),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin(),
                        btn,
                        (javafx.scene.layout.HBox) btn.getParent()
                ));

                Alert ok = new Alert(Alert.AlertType.INFORMATION,
                        "Reserva cancelada correctamente.", ButtonType.OK);
                ok.setTitle("Reserva cancelada");
                ok.setHeaderText(null);
                ok.showAndWait();
            }
        });
    }

    // ── Confirmar reserva al pulsar un slot ───────────────────────────────────
    private void confirmarReserva(Pista pista, LocalTime inicio, LocalTime fin,
                                   Button btnPulsado, HBox slots) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", pistasContainer);
            return;
        }

        // Confirmación con Alert
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Reservar %s el %s de %s a %s?\nPrecio: %.2f €",
                        pista.getNombre(),
                        fechaActual.format(DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"))),
                        inicio, fin,
                        reservaService.calcularPrecio(pista, inicio, fin)),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar reserva");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    Usuario usuario = SessionManager.getInstance().getUsuarioActual();
                    reservaService.crear(usuario, pista, fechaActual, inicio, fin);

                    // Marcar el slot como ocupado visualmente
                    btnPulsado.setText("✓\nOcupado");
                    btnPulsado.getStyleClass().remove("slot-libre");
                    btnPulsado.getStyleClass().add("slot-ocupado");
                    btnPulsado.setMouseTransparent(true);

                    // Mostrar alert de éxito
                    Alert ok = new Alert(Alert.AlertType.INFORMATION,
                            "¡Reserva confirmada! " + pista.getNombre() + " · " + inicio + " - " + fin,
                            ButtonType.OK);
                    ok.setTitle("Reserva confirmada");
                    ok.setHeaderText(null);
                    ok.showAndWait();

                } catch (IllegalArgumentException e) {
                    Alert err = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    err.setTitle("Error");
                    err.setHeaderText(null);
                    err.showAndWait();
                    // Recargar para reflejar estado real
                    cargarPistas();
                }
            }
        });
    }

    // ── Navegación por fechas ─────────────────────────────────────────────────
    @FXML
    protected void onDiaAnterior() {
        fechaActual = fechaActual.minusDays(1);
        // No permitir fechas pasadas
        if (fechaActual.isBefore(LocalDate.now())) {
            fechaActual = LocalDate.now();
        }
        actualizarFechaLabel();
        if (deporteSeleccionado != null) cargarPistas();
    }

    @FXML
    protected void onDiaSiguiente() {
        fechaActual = fechaActual.plusDays(1);
        actualizarFechaLabel();
        if (deporteSeleccionado != null) cargarPistas();
    }

    private void actualizarFechaLabel() {
        fechaLabel.setText(fechaActual.format(FMT_FECHA));
        // Deshabilitar botón anterior si ya estamos en hoy
        btnAnterior.setDisable(fechaActual.equals(LocalDate.now()));
    }

    // ── Volver ────────────────────────────────────────────────────────────────
    @FXML
    protected void onVolver() {
        NavigationUtil.navigateTo("homepage.fxml", pistasContainer);
    }
}

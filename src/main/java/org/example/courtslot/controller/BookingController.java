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

public class BookingController implements Initializable {

    @FXML private Label  fechaLabel;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private VBox   pistasContainer;
    @FXML private Label  tituloLabel;

    private Deporte    deporteSeleccionado;
    private LocalDate  fechaActual = LocalDate.now();

    private final PistaService   pistaService   = new PistaService();
    private final ReservaService reservaService = new ReservaService();

    private static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_CIERRE   = LocalTime.of(21, 0);
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actualizarFechaLabel();
    }

    public void setDeporte(Deporte deporte) {
        this.deporteSeleccionado = deporte;
        tituloLabel.setText("Reserva de Pistas · " + deporte.getNombre());
        cargarPistas();
    }

    // ── Carga de pistas ───────────────────────────────────────────────────────

    private void cargarPistas() {
        // El service filtra las pistas activas del deporte seleccionado
        List<Pista> pistasDelDeporte = pistaService.getByDeporte(deporteSeleccionado.getId());

        pistasContainer.getChildren().clear();

        if (pistasDelDeporte.isEmpty()) {
            Label empty = new Label("No hay pistas disponibles para este deporte.");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 14px;");
            pistasContainer.getChildren().add(empty);
            return;
        }

        for (Pista pista : pistasDelDeporte) {
            pistasContainer.getChildren().add(crearFilaPista(pista));
        }
    }

    // ── Construye la fila visual de una pista con sus slots horarios ──────────

    private VBox crearFilaPista(Pista pista) {
        VBox fila = new VBox(10);
        fila.getStyleClass().add("pista-row");

        Label nombre = new Label(pista.getNombre());
        nombre.getStyleClass().add("pista-nombre");

        String textoDesc = (pista.getDescripcion() != null && !pista.getDescripcion().isBlank())
                ? pista.getDescripcion()
                : String.format("%.2f €/h", pista.getPrecioPorHora());
        Label desc = new Label(textoDesc);
        desc.getStyleClass().add("pista-desc");

        VBox cabecera = new VBox(2, nombre, desc);

        HBox slots = new HBox(6);
        slots.setAlignment(Pos.CENTER_LEFT);
        slots.getStyleClass().add("slots-row");

        // El service devuelve las reservas que bloquean slots para esta pista y fecha
        List<Reserva> bloqueados = reservaService.getSlotsBloqueados(pista.getId(), fechaActual);

        Long usuarioActualId = null;
        if (SessionManager.getInstance().estaLogueado()) {
            usuarioActualId = SessionManager.getInstance().getUsuarioActual().getId();
        }

        // Recorrer hora a hora desde apertura hasta cierre
        LocalTime hora = HORA_APERTURA;
        while (hora.isBefore(HORA_CIERRE)) {
            final LocalTime slotInicio = hora;
            final LocalTime slotFin    = hora.plusHours(1);

            // Buscar si alguna reserva ocupa este slot
            Reserva reservaSolapa = null;
            for (Reserva r : bloqueados) {
                if (r.getHoraInicio().isBefore(slotFin) && r.getHoraFin().isAfter(slotInicio)) {
                    reservaSolapa = r;
                    break;
                }
            }

            Button btn = new Button();
            btn.getStyleClass().add("slot-btn");
            btn.setPrefWidth(80);
            btn.setPrefHeight(50);

            if (reservaSolapa != null) {
                boolean esMia = usuarioActualId != null
                        && reservaSolapa.getUsuario().getId().equals(usuarioActualId);

                if (esMia) {
                    btn.setText("⚑\nMi reserva");
                    btn.getStyleClass().add("slot-mio");
                    btn.setCursor(javafx.scene.Cursor.HAND);
                    final Reserva reservaACancelar = reservaSolapa;
                    btn.setOnAction(e -> cancelarMiReservaDesdeSlot(reservaACancelar, btn));
                } else {
                    btn.setText("✓\nOcupado");
                    btn.getStyleClass().add("slot-ocupado");
                    btn.setMouseTransparent(true);
                }
            } else {
                btn.setText(slotInicio.toString());
                btn.getStyleClass().add("slot-libre");
                btn.setCursor(javafx.scene.Cursor.HAND);
                btn.setOnAction(e -> confirmarReserva(pista, slotInicio, slotFin, btn));
            }

            slots.getChildren().add(btn);
            hora = hora.plusHours(1);
        }

        fila.getChildren().addAll(cabecera, slots);
        return fila;
    }

    // ── Cancelar reserva propia desde un slot naranja ─────────────────────────

    private void cancelarMiReservaDesdeSlot(Reserva reserva, Button btn) {
        DateTimeFormatter fmtCorta = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Cancelar tu reserva de %s\nel %s de %s a %s?",
                        reserva.getPista().getNombre(),
                        fechaActual.format(fmtCorta),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin()),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                reservaService.cancelar(reserva.getId());

                btn.setText(reserva.getHoraInicio().toString());
                btn.getStyleClass().remove("slot-mio");
                btn.getStyleClass().add("slot-libre");
                btn.setCursor(javafx.scene.Cursor.HAND);
                btn.setMouseTransparent(false);
                btn.setOnAction(e -> confirmarReserva(
                        reserva.getPista(),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin(),
                        btn
                ));

                new Alert(Alert.AlertType.INFORMATION, "Reserva cancelada correctamente.", ButtonType.OK).showAndWait();
            }
        });
    }

    // ── Confirmar reserva al pulsar un slot libre ─────────────────────────────

    private void confirmarReserva(Pista pista, LocalTime inicio, LocalTime fin, Button btnPulsado) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", pistasContainer);
            return;
        }

        DateTimeFormatter fmtCorta = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"));
        double precio = reservaService.calcularPrecio(pista, inicio, fin);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Reservar %s el %s de %s a %s?\nPrecio: %.2f €",
                        pista.getNombre(),
                        fechaActual.format(fmtCorta),
                        inicio, fin,
                        precio),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar reserva");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    Usuario usuario = SessionManager.getInstance().getUsuarioActual();
                    reservaService.crear(usuario, pista, fechaActual, inicio, fin);

                    btnPulsado.setText("✓\nOcupado");
                    btnPulsado.getStyleClass().remove("slot-libre");
                    btnPulsado.getStyleClass().add("slot-ocupado");
                    btnPulsado.setMouseTransparent(true);

                    new Alert(Alert.AlertType.INFORMATION,
                            "¡Reserva confirmada! " + pista.getNombre() + " · " + inicio + " - " + fin,
                            ButtonType.OK).showAndWait();

                } catch (IllegalArgumentException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
                    cargarPistas();
                }
            }
        });
    }

    // ── Navegación por fechas ─────────────────────────────────────────────────

    @FXML
    protected void onDiaAnterior() {
        fechaActual = fechaActual.minusDays(1);
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
        btnAnterior.setDisable(fechaActual.equals(LocalDate.now()));
    }

    @FXML
    protected void onVolver() {
        NavigationUtil.navigateTo("homepage.fxml", pistasContainer);
    }
}
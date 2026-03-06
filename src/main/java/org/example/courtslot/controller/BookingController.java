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
import java.util.Optional;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML private Label  fechaLabel;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private VBox   pistasContainer;
    @FXML private Label  tituloLabel;

    private Deporte   deporteSeleccionado;
    private LocalDate fechaActual = LocalDate.now();

    private final PistaService   pistaService   = new PistaService();
    private final ReservaService reservaService = new ReservaService();

    private static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_CIERRE   = LocalTime.of(21, 0);
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter FMT_CORTA =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actualizarFechaLabel();
    }

    // Recibe el deporte desde HomepageController y carga la pantalla
    public void setDeporte(Deporte deporte) {
        this.deporteSeleccionado = deporte;
        tituloLabel.setText("Reserva de Pistas · " + deporte.getNombre());
        cargarPistas();
    }

    // ── Rellena el contenedor con una fila por cada pista ────────────────────

    private void cargarPistas() {
        pistasContainer.getChildren().clear();

        List<Pista> pistasDelDeporte = pistaService.getByDeporte(deporteSeleccionado.getId());

        if (pistasDelDeporte.isEmpty()) {
            Label aviso = new Label("No hay pistas disponibles para este deporte.");
            pistasContainer.getChildren().add(aviso);
            return;
        }

        for (Pista pista : pistasDelDeporte) {
            VBox filaPista = crearFilaPista(pista);
            pistasContainer.getChildren().add(filaPista);
        }
    }

    // ── Crea la fila visual de una pista: nombre + botones de hora ───────────

    private VBox crearFilaPista(Pista pista) {

        // Etiqueta con el nombre de la pista
        Label lblNombre = new Label(pista.getNombre());

        // Etiqueta con descripción o precio si no hay descripción
        String textoDesc;
        if (pista.getDescripcion() != null && !pista.getDescripcion().isBlank()) {
            textoDesc = pista.getDescripcion();
        } else {
            textoDesc = String.format("%.2f €/h", pista.getPrecioPorHora());
        }
        Label lblDesc = new Label(textoDesc);

        VBox cabecera = new VBox(2, lblNombre, lblDesc);

        // Fila de botones de hora
        HBox filaSlots = new HBox(6);
        filaSlots.setAlignment(Pos.CENTER_LEFT);

        // Reservas que ya ocupan slots en esta pista y fecha
        List<Reserva> reservasOcupadas = reservaService.getSlotsBloqueados(pista.getId(), fechaActual);

        // ID del usuario logueado para saber si una reserva es suya
        Long idUsuarioActual = null;
        if (SessionManager.getInstance().estaLogueado()) {
            idUsuarioActual = SessionManager.getInstance().getUsuarioActual().getId();
        }

        // Crear un botón por cada hora entre apertura y cierre
        LocalTime hora = HORA_APERTURA;
        while (hora.isBefore(HORA_CIERRE)) {

            LocalTime slotInicio = hora;
            LocalTime slotFin    = hora.plusHours(1);

            // Buscar si este slot está ocupado por alguna reserva
            Reserva reservaEnEsteSlot = null;
            for (Reserva r : reservasOcupadas) {
                if (r.getHoraInicio().isBefore(slotFin) && r.getHoraFin().isAfter(slotInicio)) {
                    reservaEnEsteSlot = r;
                    break;
                }
            }

            Button btnSlot = crearBotonSlot(pista, slotInicio, slotFin, reservaEnEsteSlot, idUsuarioActual);
            filaSlots.getChildren().add(btnSlot);

            hora = hora.plusHours(1);
        }

        VBox fila = new VBox(10, cabecera, filaSlots);
        return fila;
    }

    // ── Crea un botón de slot según su estado: libre, mío u ocupado ──────────

    private Button crearBotonSlot(Pista pista, LocalTime slotInicio, LocalTime slotFin,
                                  Reserva reservaEnEsteSlot, Long idUsuarioActual) {
        Button btn = new Button();
        btn.setPrefWidth(80);
        btn.setPrefHeight(50);

        if (reservaEnEsteSlot == null) {
            // Slot libre — se puede reservar
            btn.setText(slotInicio.toString());
            btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
            btn.setOnAction(e -> onClickSlotLibre(pista, slotInicio, slotFin, btn));

        } else if (idUsuarioActual != null && reservaEnEsteSlot.getUsuario().getId().equals(idUsuarioActual)) {
            // Slot con mi propia reserva — se puede cancelar
            btn.setText("Mi reserva\n" + slotInicio);
            btn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
            Reserva reservaACancelar = reservaEnEsteSlot;
            btn.setOnAction(e -> onClickMiReserva(reservaACancelar, btn, slotInicio, slotFin, pista));

        } else {
            // Slot ocupado por otro usuario — no se puede hacer nada
            btn.setText("Ocupado\n" + slotInicio);
            btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
            btn.setDisable(true);
        }

        return btn;
    }

    // ── Al pulsar un slot libre: confirmar y crear la reserva ────────────────

    private void onClickSlotLibre(Pista pista, LocalTime inicio, LocalTime fin, Button btnPulsado) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", pistasContainer);
            return;
        }

        double precio = reservaService.calcularPrecio(pista, inicio, fin);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Reservar " + pista.getNombre() + " el " + fechaActual.format(FMT_CORTA)
                        + " de " + inicio + " a " + fin + "?\nPrecio: " + String.format("%.2f €", precio),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar reserva");
        confirm.setHeaderText(null);

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            try {
                Usuario usuario = SessionManager.getInstance().getUsuarioActual();
                reservaService.crear(usuario, pista, fechaActual, inicio, fin);

                // Marcar el botón como ocupado visualmente
                btnPulsado.setText("Ocupado\n" + inicio);
                btnPulsado.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                btnPulsado.setDisable(true);

                new Alert(Alert.AlertType.INFORMATION,
                        "¡Reserva confirmada! " + pista.getNombre() + " · " + inicio + " - " + fin,
                        ButtonType.OK).showAndWait();

            } catch (IllegalArgumentException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
                cargarPistas();
            }
        }
    }

    // ── Al pulsar mi reserva (naranja): confirmar cancelación ────────────────

    private void onClickMiReserva(Reserva reserva, Button btn,
                                  LocalTime slotInicio, LocalTime slotFin, Pista pista) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar tu reserva de " + reserva.getPista().getNombre()
                        + " el " + fechaActual.format(FMT_CORTA)
                        + " de " + reserva.getHoraInicio() + " a " + reserva.getHoraFin() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText(null);

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            reservaService.cancelar(reserva.getId());

            // Volver a mostrar el botón como libre
            btn.setText(slotInicio.toString());
            btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
            btn.setDisable(false);
            btn.setOnAction(e -> onClickSlotLibre(pista, slotInicio, slotFin, btn));

            new Alert(Alert.AlertType.INFORMATION, "Reserva cancelada.", ButtonType.OK).showAndWait();
        }
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
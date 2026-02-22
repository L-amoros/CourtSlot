package org.example.courtslot.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.courtslot.model.Reserva;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MyBookingsController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label emptyLabel;

    @FXML private TableView<Reserva>          reservasTable;
    @FXML private TableColumn<Reserva,String> colPista;
    @FXML private TableColumn<Reserva,String> colDeporte;
    @FXML private TableColumn<Reserva,String> colFecha;
    @FXML private TableColumn<Reserva,String> colHora;
    @FXML private TableColumn<Reserva,String> colPrecio;
    @FXML private TableColumn<Reserva,String> colEstado;

    private final ReservaService reservaService = new ReservaService();
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nombre del usuario en el chip de la navbar
        if (SessionManager.getInstance().estaLogueado()) {
            userNameLabel.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }

        configurarTabla();
        cargarReservas();
    }

    // ── Configurar columnas ───────────────────────────────────────────────────

    private void configurarTabla() {

        colPista.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPista().getNombre()));

        colDeporte.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPista().getDeporte().getNombre()));

        colFecha.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFecha().format(FMT_FECHA)));

        colHora.setCellValueFactory(cell -> {
            Reserva r = cell.getValue();
            return new SimpleStringProperty(r.getHoraInicio() + " – " + r.getHoraFin());
        });

        colPrecio.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f €", cell.getValue().getPrecioTotal())));

        // Estado con color según valor
        colEstado.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEstado().name()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "CONFIRMADA" -> setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    case "CANCELADA"  -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    default           -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });
    }

    // ── Cargar reservas del usuario logueado ──────────────────────────────────

    private void cargarReservas() {
        Long usuarioId = SessionManager.getInstance().getUsuarioActual().getId();
        List<Reserva> reservas = reservaService.getMisReservas(usuarioId);

        if (reservas.isEmpty()) {
            emptyLabel.setVisible(true);
            reservasTable.setVisible(false);
        } else {
            emptyLabel.setVisible(false);
            reservasTable.setVisible(true);
            reservasTable.setItems(FXCollections.observableArrayList(reservas));
        }
    }

    // ── Cancelar reserva seleccionada ─────────────────────────────────────────

    @FXML
    protected void onCancelarReserva() {
        Reserva sel = reservasTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una reserva primero.", ButtonType.OK)
                    .showAndWait();
            return;
        }
        if (sel.getEstado() == Reserva.Estado.CANCELADA) {
            new Alert(Alert.AlertType.INFORMATION, "Esta reserva ya está cancelada.", ButtonType.OK)
                    .showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Cancelar la reserva de %s el %s de %s a %s?",
                        sel.getPista().getNombre(),
                        sel.getFecha().format(FMT_FECHA),
                        sel.getHoraInicio(),
                        sel.getHoraFin()),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                reservaService.cancelar(sel.getId());
                cargarReservas();
            }
        });
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    @FXML
    protected void onVolver() {
        NavigationUtil.navigateTo("homepage.fxml", reservasTable);
    }
}

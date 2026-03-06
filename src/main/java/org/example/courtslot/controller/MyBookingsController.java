package org.example.courtslot.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.courtslot.model.Reserva;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private DatePicker       filtroFecha;

    private final ReservaService reservaService = new ReservaService();
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES"));

    private List<Reserva> todasLasReservas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.getInstance().estaLogueado()) {
            userNameLabel.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }

        configurarTabla();
        configurarFiltros();
        cargarReservas();
    }

    private void configurarTabla() {
        colPista.setCellValueFactory(new PropertyValueFactory<>("nombrePista"));
        colDeporte.setCellValueFactory(new PropertyValueFactory<>("nombreDeporte"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioFormateado"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
    }


    private void configurarFiltros() {
        filtroEstado.getItems().addAll("Todos", "CONFIRMADA", "CANCELADA");
        filtroEstado.setValue("Todos");

        // Cualquier cambio en los 3 filtros llama a aplicarFiltros()
        searchField.textProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
        filtroFecha.valueProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
    }


    private void cargarReservas() {
        Long usuarioId = SessionManager.getInstance().getUsuarioActual().getId();
        todasLasReservas = reservaService.getMisReservas(usuarioId);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String texto      = searchField.getText().toLowerCase().trim();
        String estado     = filtroEstado.getValue();
        LocalDate fecha   = filtroFecha.getValue();

        List<Reserva> filtradas = todasLasReservas.stream()
                .filter(r -> {
                    boolean coincideTexto =
                            texto.isEmpty()
                            || r.getNombrePista().toLowerCase().contains(texto)
                            || r.getNombreDeporte().toLowerCase().contains(texto);
                    boolean coincideEstado =
                            estado == null
                            || estado.equals("Todos")
                            || r.getEstado().name().equals(estado);
                    boolean coincideFecha =
                            fecha == null
                            || r.getFecha().equals(fecha);
                    return coincideTexto && coincideEstado && coincideFecha;
                })
                .collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            emptyLabel.setVisible(true);
            reservasTable.setVisible(false);
        } else {
            emptyLabel.setVisible(false);
            reservasTable.setVisible(true);
            reservasTable.setItems(FXCollections.observableArrayList(filtradas));
        }
    }


    @FXML
    protected void onLimpiarFiltros() {
        searchField.clear();
        filtroEstado.setValue("Todos");
        filtroFecha.setValue(null);
    }

    @FXML
    protected void onCancelarReserva() {
        Reserva sel = reservasTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una reserva primero.", ButtonType.OK).showAndWait();
            return;
        }
        if (sel.getEstado() == Reserva.Estado.CANCELADA) {
            new Alert(Alert.AlertType.INFORMATION, "Esta reserva ya está cancelada.", ButtonType.OK).showAndWait();
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

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            reservaService.cancelar(sel.getId());
            cargarReservas();
        }
    }

    @FXML
    protected void onVolver() {
        NavigationUtil.navigateTo("homepage.fxml", reservasTable);
    }
}
package org.example.courtslot.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.example.courtslot.model.Deporte;
import org.example.courtslot.model.Pista;
import org.example.courtslot.model.Reserva;
import org.example.courtslot.service.DeporteService;
import org.example.courtslot.service.PistaService;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminPanelController implements Initializable {

    // ══ TAB PISTAS ════════════════════════════════════════════════════════════
    @FXML private TableView<Pista>          pistasTable;
    @FXML private TableColumn<Pista,String> colPistaNombre;
    @FXML private TableColumn<Pista,String> colPistaDeporte;
    @FXML private TableColumn<Pista,String> colPistaDesc;
    @FXML private TableColumn<Pista,String> colPistaPrecio;
    @FXML private TableColumn<Pista,String> colPistaActiva;

    @FXML private TextField        pistaNombreField;
    @FXML private TextField        pistaDescField;
    @FXML private TextField        pistaPrecioField;
    @FXML private TextField        pistaImagenField;
    @FXML private ComboBox<String> pistaDeporteCombo;
    @FXML private ComboBox<String> pistaEstadoCombo;
    @FXML private Button           pistaSaveBtn;
    @FXML private Label            pistaErrorLabel;

    // ══ TAB DEPORTES ══════════════════════════════════════════════════════════
    @FXML private TableView<Deporte>          deportesTable;
    @FXML private TableColumn<Deporte,String> colDeporteNombre;
    @FXML private TableColumn<Deporte,String> colDeporteDesc;
    @FXML private TableColumn<Deporte,String> colDeporteEstado;

    @FXML private TextField deporteNombreField;
    @FXML private TextField deporteDescField;
    @FXML private Button    deporteSaveBtn;
    @FXML private Label     deporteErrorLabel;

    // ══ TAB RESERVAS ══════════════════════════════════════════════════════════
    @FXML private TableView<Reserva>           reservasTable;
    @FXML private TableColumn<Reserva,String>  colResUsuario;
    @FXML private TableColumn<Reserva,String>  colResPista;
    @FXML private TableColumn<Reserva,String>  colResDeporte;
    @FXML private TableColumn<Reserva,String>  colResFecha;
    @FXML private TableColumn<Reserva,String>  colResHora;
    @FXML private TableColumn<Reserva,String>  colResPrecio;
    @FXML private TableColumn<Reserva,String>  colResEstado;

    // ══ Servicios ═════════════════════════════════════════════════════════════
    private final PistaService   pistaService   = new PistaService();
    private final DeporteService deporteService = new DeporteService();
    private final ReservaService reservaService = new ReservaService();

    private Pista pistaEditando = null;

    // ══ initialize ════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pistaErrorLabel.setVisible(false);
        deporteErrorLabel.setVisible(false);

        pistaEstadoCombo.getItems().addAll("ACTIVA", "MANTENIMIENTO", "DESACTIVADA");
        pistaEstadoCombo.setValue("ACTIVA");

        configurarTablaPistas();
        configurarTablaDeportes();
        configurarTablaReservas();

        cargarDeportesEnCombo();
        cargarPistas();
        cargarDeportes();
        cargarReservas();
    }

    // ══ PISTAS ════════════════════════════════════════════════════════════════

    private void configurarTablaPistas() {
        colPistaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPistaDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPistaDeporte.setCellValueFactory(new PropertyValueFactory<>("nombreDeporte"));
        colPistaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioFormateado"));
        colPistaActiva.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
    }

    private void cargarPistas() {
        pistasTable.setItems(FXCollections.observableArrayList(pistaService.getAllIncludingDesactivadas()));

        // Doble clic en una fila carga la pista en el formulario para editarla
        pistasTable.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Pista sel = pistasTable.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    cargarPistaEnFormulario(sel);
                }
            }
        });
    }

    private void cargarPistaEnFormulario(Pista pista) {
        pistaEditando = pista;
        pistaNombreField.setText(pista.getNombre());
        pistaDescField.setText(pista.getDescripcion() != null ? pista.getDescripcion() : "");
        pistaPrecioField.setText(String.valueOf(pista.getPrecioPorHora()));
        pistaImagenField.setText(pista.getImagenUrl() != null ? pista.getImagenUrl() : "");
        pistaDeporteCombo.setValue(pista.getDeporte().getNombre());
        pistaEstadoCombo.setValue(pista.getEstado().name());
        pistaSaveBtn.setText("💾 Actualizar Pista");
    }

    @FXML
    protected void onGuardarPista() {
        pistaErrorLabel.setVisible(false);

        if (pistaNombreField.getText().trim().isEmpty()) {
            mostrarErrorPista("El nombre de la pista no puede estar vacío.");
            return;
        }
        if (pistaPrecioField.getText().trim().isEmpty()) {
            mostrarErrorPista("El precio no puede estar vacío.");
            return;
        }
        if (pistaDeporteCombo.getValue() == null) {
            mostrarErrorPista("Debes seleccionar un deporte.");
            return;
        }

        try {
            double precio = Double.parseDouble(pistaPrecioField.getText().replace(",", "."));

            Optional<Deporte> deporteOpt = deporteService.findByNombre(pistaDeporteCombo.getValue());
            if (deporteOpt.isEmpty()) {
                mostrarErrorPista("Deporte no encontrado.");
                return;
            }
            Deporte deporte = deporteOpt.get();
            Pista.Estado estado = Pista.Estado.valueOf(pistaEstadoCombo.getValue());

            if (pistaEditando != null) {
                pistaEditando.setNombre(pistaNombreField.getText().trim());
                pistaEditando.setDescripcion(pistaDescField.getText().trim());
                pistaEditando.setPrecioPorHora(precio);
                pistaEditando.setImagenUrl(pistaImagenField.getText().trim());
                pistaEditando.setDeporte(deporte);
                pistaEditando.setEstado(estado);
                pistaService.update(pistaEditando);
                pistaEditando = null;
                pistaSaveBtn.setText("💾 Guardar Pista");
            } else {
                Pista nueva = new Pista(
                        pistaNombreField.getText().trim(),
                        pistaDescField.getText().trim(),
                        precio,
                        pistaImagenField.getText().trim(),
                        deporte
                );
                nueva.setEstado(estado);
                pistaService.save(nueva);
            }

            limpiarFormPista();
            cargarPistas();

        } catch (NumberFormatException e) {
            mostrarErrorPista("El precio debe ser un número válido (ej: 15.00)");
        } catch (IllegalArgumentException e) {
            mostrarErrorPista(e.getMessage());
        }
    }

    @FXML
    protected void onEliminarPista() {
        Pista sel = pistasTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarErrorPista("Selecciona una pista de la tabla primero.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la pista '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            pistaService.delete(sel.getId());
            limpiarFormPista();
            cargarPistas();
            cargarReservas();
        }
    }

    private void limpiarFormPista() {
        pistaNombreField.clear();
        pistaDescField.clear();
        pistaPrecioField.clear();
        pistaImagenField.clear();
        pistaDeporteCombo.setValue(null);
        pistaEstadoCombo.setValue("ACTIVA");
        pistaEditando = null;
        pistaSaveBtn.setText("💾 Guardar Pista");
    }

    private void mostrarErrorPista(String msg) {
        pistaErrorLabel.setText(msg);
        pistaErrorLabel.setVisible(true);
    }

    // ══ DEPORTES ══════════════════════════════════════════════════════════════

    private void configurarTablaDeportes() {
        colDeporteNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDeporteDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Esta columna necesita Callback porque consulta el service, no un getter del modelo
        colDeporteEstado.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Deporte, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Deporte, String> cellData) {
                Deporte deporte = cellData.getValue();
                int numPistas = pistaService.contarPorDeporte(deporte.getId());
                return new SimpleStringProperty(numPistas + " pista(s)");
            }
        });
    }

    private void cargarDeportes() {
        deportesTable.setItems(FXCollections.observableArrayList(deporteService.getAll()));
    }

    private void cargarDeportesEnCombo() {
        pistaDeporteCombo.getItems().clear();
        List<Deporte> deportes = deporteService.getAll();
        for (Deporte d : deportes) {
            pistaDeporteCombo.getItems().add(d.getNombre());
        }
    }

    @FXML
    protected void onGuardarDeporte() {
        deporteErrorLabel.setVisible(false);

        if (deporteNombreField.getText().trim().isEmpty()) {
            deporteErrorLabel.setText("El nombre del deporte no puede estar vacío.");
            deporteErrorLabel.setVisible(true);
            return;
        }

        try {
            Deporte nuevo = new Deporte(
                    deporteNombreField.getText().trim(),
                    deporteDescField.getText().trim(),
                    null
            );
            deporteService.save(nuevo);
            deporteNombreField.clear();
            deporteDescField.clear();
            cargarDeportes();
            cargarDeportesEnCombo();
        } catch (IllegalArgumentException e) {
            deporteErrorLabel.setText(e.getMessage());
            deporteErrorLabel.setVisible(true);
        }
    }

    @FXML
    protected void onEliminarDeporte() {
        Deporte sel = deportesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            deporteErrorLabel.setText("Selecciona un deporte primero.");
            deporteErrorLabel.setVisible(true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar '" + sel.getNombre() + "'? Se eliminarán sus pistas y reservas.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            deporteService.delete(sel.getId());
            cargarDeportes();
            cargarDeportesEnCombo();
            cargarPistas();
            cargarReservas();
        }
    }

    // ══ RESERVAS ══════════════════════════════════════════════════════════════

    private void configurarTablaReservas() {
        colResUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colResPista.setCellValueFactory(new PropertyValueFactory<>("nombrePista"));
        colResDeporte.setCellValueFactory(new PropertyValueFactory<>("nombreDeporte"));
        colResFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colResHora.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colResPrecio.setCellValueFactory(new PropertyValueFactory<>("precioFormateado"));
        colResEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
    }

    private void cargarReservas() {
        reservasTable.setItems(FXCollections.observableArrayList(reservaService.getAll()));
    }

    @FXML
    protected void onCancelarReserva() {
        Reserva sel = reservasTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (sel.getEstado() == Reserva.Estado.CANCELADA) {
            new Alert(Alert.AlertType.INFORMATION, "Esta reserva ya está cancelada.", ButtonType.OK).showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar la reserva de " + sel.getUsuario().getNombre() + " — " + sel.getPista().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            reservaService.cancelar(sel.getId());
            cargarReservas();
        }
    }

    // ══ Navegación ════════════════════════════════════════════════════════════
    @FXML
    protected void onVolver() {
        NavigationUtil.navigateTo("homepage.fxml", pistasTable);
    }
}
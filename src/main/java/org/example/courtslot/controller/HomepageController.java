package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.courtslot.HelloApplication;
import org.example.courtslot.model.Deporte;
import org.example.courtslot.model.Pista;
import org.example.courtslot.service.DeporteService;
import org.example.courtslot.service.PistaService;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class HomepageController implements Initializable {

    // ── Navbar ────────────────────────────────────────────────────────────────
    @FXML private Label     userNameLabel;
    @FXML private Button    adminBtn;
    @FXML private TextField searchField;

    // ── Layout principal ──────────────────────────────────────────────────────
    @FXML private VBox      adminSidebar;
    @FXML private FlowPane  cardsContainer;

    // ── Widgets de estadísticas admin ──────────────────────────────────────────
    @FXML private Label statReservadasNum;
    @FXML private Label statReservadasSub;
    @FXML private Label statDisponiblesNum;
    @FXML private Label statDisponiblesSub;
    @FXML private Label statHorarioNum;

    private final DeporteService deporteService = new DeporteService();
    private final PistaService   pistaService   = new PistaService();
    private final ReservaService reservaService = new ReservaService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.getInstance().estaLogueado()) {
            userNameLabel.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }

        boolean esAdmin = SessionManager.getInstance().esAdmin();
        adminBtn.setVisible(esAdmin);
        adminBtn.setManaged(esAdmin);
        adminSidebar.setVisible(esAdmin);
        adminSidebar.setManaged(esAdmin);

        if (esAdmin) {
            cargarEstadisticasAdmin();
        }

        cargarDeportes(null);
    }

    // ── Carga cards por DEPORTE ───────────────────────────────────────────────

    private void cargarDeportes(String filtroNombre) {
        cardsContainer.getChildren().clear();
        List<Deporte> deportes = deporteService.getAll();

        for (Deporte deporte : deportes) {
            if (filtroNombre != null && !filtroNombre.isBlank() &&
                    !deporte.getNombre().toLowerCase().contains(filtroNombre.toLowerCase())) {
                continue;
            }
            cardsContainer.getChildren().add(crearCardDeporte(deporte));
        }

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("No se encontraron deportes.");
            noResults.getStyleClass().add("no-results-label");
            cardsContainer.getChildren().add(noResults);
        }
    }

    private VBox crearCardDeporte(Deporte deporte) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefWidth(230);
        card.setMaxWidth(230);
        card.setMinWidth(200);

        // ── Imagen + badge ────────────────────────────────────────────────────
        StackPane mediaPane = new StackPane();
        mediaPane.getStyleClass().add("card-media");
        mediaPane.setPrefHeight(160);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        cargarImagenDeporte(imageView, deporte);

        Label corazon = new Label("♥");
        corazon.getStyleClass().add("heart-btn");
        StackPane.setAlignment(corazon, Pos.BOTTOM_RIGHT);
        corazon.setTranslateX(-10);
        corazon.setTranslateY(-10);

        // El service dice cuántas pistas activas tiene el deporte
        int pistasActivas = pistaService.contarPorDeporte(deporte.getId());

        if (pistasActivas > 0) {
            Label badge = new Label("Disponible");
            badge.getStyleClass().addAll("badge", "badge-available");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            badge.setTranslateX(-10);
            badge.setTranslateY(10);
            mediaPane.getChildren().addAll(imageView, badge, corazon);
        } else {
            mediaPane.getChildren().addAll(imageView, corazon);
        }

        // ── Cuerpo de la card ─────────────────────────────────────────────────
        VBox body = new VBox(6);
        body.getStyleClass().add("card-body");

        Label titulo = new Label(deporte.getNombre());
        titulo.getStyleClass().add("card-title");

        if (deporte.getDescripcion() != null && !deporte.getDescripcion().isBlank()) {
            Label desc = new Label(deporte.getDescripcion());
            desc.getStyleClass().add("card-subtitle");
            desc.setWrapText(true);
            body.getChildren().addAll(titulo, desc);
        } else {
            body.getChildren().add(titulo);
        }

        // El service devuelve directamente la pista más barata del deporte
        Pista pistaMasBarata = pistaService.getPistaMasBarata(deporte.getId());
        if (pistaMasBarata != null) {
            Label precio = new Label(String.format("%.2f €/h", pistaMasBarata.getPrecioPorHora()));
            precio.getStyleClass().add("card-price");
            body.getChildren().add(precio);
        }

        Button reservarBtn = new Button("Reservar");
        reservarBtn.getStyleClass().add("btn-reservar");
        reservarBtn.setMaxWidth(Double.MAX_VALUE);
        reservarBtn.setCursor(javafx.scene.Cursor.HAND);
        reservarBtn.setOnAction(e -> abrirSeleccionPista(deporte));
        VBox.setMargin(reservarBtn, new javafx.geometry.Insets(8, 0, 0, 0));

        body.getChildren().add(reservarBtn);
        card.getChildren().addAll(mediaPane, body);
        return card;
    }

    private void cargarImagenDeporte(ImageView iv, Deporte deporte) {
        if (deporte.getIcono() != null && !deporte.getIcono().isBlank()) {
            URL imgUrl = HelloApplication.class.getResource("images/" + deporte.getIcono());
            if (imgUrl != null) {
                iv.setImage(new Image(imgUrl.toString()));
                return;
            }
        }
        String nombreImagen = deporte.getNombre().toLowerCase()
                .replace(" ", "-")
                .replace("á","a").replace("é","e").replace("í","i")
                .replace("ó","o").replace("ú","u") + ".png";
        URL imgUrl = HelloApplication.class.getResource("images/" + nombreImagen);
        if (imgUrl != null) {
            iv.setImage(new Image(imgUrl.toString()));
        }
    }

    private void abrirSeleccionPista(Deporte deporte) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", cardsContainer);
            return;
        }

        // El service filtra las pistas del deporte
        List<Pista> pistasDelDeporte = pistaService.getByDeporte(deporte.getId());

        if (pistasDelDeporte.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                    "No hay pistas disponibles para " + deporte.getNombre() + " en este momento.",
                    ButtonType.OK);
            alerta.setTitle("Sin pistas disponibles");
            alerta.showAndWait();
            return;
        }

        BookingController ctrl = NavigationUtil.navigateToAndGetController("booking.fxml", cardsContainer);
        if (ctrl != null) {
            ctrl.setDeporte(deporte);
        }
    }

    // ── Buscar ────────────────────────────────────────────────────────────────
    @FXML
    protected void onBuscar() {
        cargarDeportes(searchField.getText().trim());
    }

    // ── Estadísticas admin ────────────────────────────────────────────────────
    private void cargarEstadisticasAdmin() {
        List<Pista> todasPistas = pistaService.getAll();
        LocalDate   hoy         = LocalDate.now();

        // El service cuenta las reservas de hoy
        int reservadasHoy   = reservaService.contarReservasEnFecha(hoy);
        int disponiblesHoy  = todasPistas.size() - reservadasHoy;
        if (disponiblesHoy < 0) disponiblesHoy = 0;

        statReservadasNum.setText(String.valueOf(reservadasHoy));
        statReservadasSub.setText("de " + todasPistas.size() + " pistas totales");
        statDisponiblesNum.setText(String.valueOf(disponiblesHoy));
        statDisponiblesSub.setText(reservadasHoy + " ocupadas");

        // El service calcula la franja horaria más reservada
        statHorarioNum.setText(reservaService.getFranjaHorariaMasReservada());
    }

    // ── Navegación ────────────────────────────────────────────────────────────
    @FXML
    protected void onMisReservas() {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", cardsContainer);
            return;
        }
        NavigationUtil.navigateTo("my-bookings.fxml", cardsContainer);
    }

    @FXML
    protected void onAdminPanel() {
        NavigationUtil.navigateTo("admin-panel.fxml", cardsContainer);
    }

    @FXML
    protected void onCerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
        NavigationUtil.navigateTo("login.fxml", cardsContainer);
    }
}
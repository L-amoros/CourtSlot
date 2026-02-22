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
import org.example.courtslot.model.Reserva;
import org.example.courtslot.service.DeporteService;
import org.example.courtslot.service.PistaService;
import org.example.courtslot.service.ReservaService;
import org.example.courtslot.util.NavigationUtil;
import org.example.courtslot.util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomepageController implements Initializable {

    // ── Navbar ────────────────────────────────────────────────────────────────
    @FXML private Label     userNameLabel;
    @FXML private Button    adminBtn;
    @FXML private TextField searchField;

    // ── Layout principal ──────────────────────────────────────────────────────
    @FXML private HBox      mainLayout;    // contiene sidebar + contenido central
    @FXML private VBox      adminSidebar;  // panel izquierdo solo para admin
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
        // Nombre del usuario en el chip verde
        if (SessionManager.getInstance().estaLogueado()) {
            userNameLabel.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }

        // Botón Admin — solo visible si el usuario es admin
        boolean esAdmin = SessionManager.getInstance().esAdmin();
        adminBtn.setVisible(esAdmin);
        adminBtn.setManaged(esAdmin);

        // Panel lateral admin — solo visible y ocupando espacio si es admin
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

    /**
     * Crea la card visual de un Deporte.
     * Imagen → nombre deporte → corazón favorito → botón Reservar
     */
    private VBox crearCardDeporte(Deporte deporte) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefWidth(230);
        card.setMaxWidth(230);
        card.setMinWidth(200);

        // ── Imagen + corazón ──────────────────────────────────────────────────
        StackPane mediaPane = new StackPane();
        mediaPane.getStyleClass().add("card-media");
        mediaPane.setPrefHeight(160);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        // Intentar cargar imagen por nombre del deporte o icono
        cargarImagenDeporte(imageView, deporte);

        // Corazón favorito (esquina inferior derecha sobre la imagen)
        Label corazon = new Label("♥");
        corazon.getStyleClass().add("heart-btn");
        StackPane.setAlignment(corazon, Pos.BOTTOM_RIGHT);
        corazon.setTranslateX(-10);
        corazon.setTranslateY(-10);

        // Badge "Disponible" — solo si hay pistas activas del deporte
        long pistasActivas = pistaService.getAll().stream()
                .filter(p -> p.getDeporte().getId().equals(deporte.getId()))
                .count();
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

        // ── Cuerpo de la card ────────────────────────────────────────────────
        VBox body = new VBox(6);
        body.getStyleClass().add("card-body");

        Label titulo = new Label(deporte.getNombre());
        titulo.getStyleClass().add("card-title");

        // Descripción si existe
        if (deporte.getDescripcion() != null && !deporte.getDescripcion().isBlank()) {
            Label desc = new Label(deporte.getDescripcion());
            desc.getStyleClass().add("card-subtitle");
            desc.setWrapText(true);
            body.getChildren().addAll(titulo, desc);
        } else {
            body.getChildren().add(titulo);
        }

        // Precio mínimo del deporte (de sus pistas)
        pistaService.getAll().stream()
                .filter(p -> p.getDeporte().getId().equals(deporte.getId()))
                .min(Comparator.comparingDouble(Pista::getPrecioPorHora))
                .ifPresent(p -> {
                    Label precio = new Label(String.format("%.2f €/h", p.getPrecioPorHora()));
                    precio.getStyleClass().add("card-price");
                    body.getChildren().add(precio);
                });

        // Botón Reservar
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

    /**
     * Carga imagen del deporte:
     * 1. Busca por icono guardado en el modelo (ej. "tenis.png")
     * 2. Si no, busca por nombre del deporte en minúsculas (ej. "futbol.png")
     */
    private void cargarImagenDeporte(ImageView iv, Deporte deporte) {
        // Intentar con el campo icono del deporte
        if (deporte.getIcono() != null && !deporte.getIcono().isBlank()) {
            URL imgUrl = HelloApplication.class.getResource("images/" + deporte.getIcono());
            if (imgUrl != null) {
                iv.setImage(new Image(imgUrl.toString()));
                return;
            }
        }
        // Fallback: nombre del deporte en minúsculas + .png
        String nombreImagen = deporte.getNombre().toLowerCase()
                .replace(" ", "-")
                .replace("á","a").replace("é","e").replace("í","i")
                .replace("ó","o").replace("ú","u") + ".png";
        URL imgUrl = HelloApplication.class.getResource("images/" + nombreImagen);
        if (imgUrl != null) {
            iv.setImage(new Image(imgUrl.toString()));
        }
    }

    // ── Al pulsar "Reservar" en una card de deporte ───────────────────────────
    /**
     * Si el deporte tiene solo 1 pista → va directo a booking.
     * Si tiene varias → navega a la lista de pistas de ese deporte.
     */
    private void abrirSeleccionPista(Deporte deporte) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", cardsContainer);
            return;
        }

        List<Pista> pistas = pistaService.getAll().stream()
                .filter(p -> p.getDeporte().getId().equals(deporte.getId()))
                .collect(Collectors.toList());

        if (pistas.isEmpty()) {
            // No hay pistas disponibles
            Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                    "No hay pistas disponibles para " + deporte.getNombre() + " en este momento.",
                    ButtonType.OK);
            alerta.setTitle("Sin pistas disponibles");
            alerta.showAndWait();
            return;
        }

        // Siempre pasamos el deporte — BookingController muestra todas sus pistas
        BookingController ctrl = NavigationUtil.navigateToAndGetController("booking.fxml", cardsContainer);
        if (ctrl != null) ctrl.setDeporte(deporte);
    }

    // ── Buscar ────────────────────────────────────────────────────────────────
    @FXML
    protected void onBuscar() {
        cargarDeportes(searchField.getText().trim());
    }

    // ── Estadísticas admin (panel lateral izquierdo) ──────────────────────────
    private void cargarEstadisticasAdmin() {
        List<Pista>   todasPistas   = pistaService.getAll();
        List<Reserva> todasReservas = reservaService.getAll();
        LocalDate     hoy           = LocalDate.now();

        // ── Stat 1: Pistas reservadas hoy ────────────────────────────────────
        long reservadasHoy = todasReservas.stream()
                .filter(r -> r.getFecha().equals(hoy))
                .filter(r -> r.getEstado() != Reserva.Estado.CANCELADA)
                .map(r -> r.getPista().getId())
                .distinct()
                .count();
        statReservadasNum.setText(String.valueOf(reservadasHoy));
        statReservadasSub.setText("de " + todasPistas.size() + " pistas totales");

        // ── Stat 2: Pistas disponibles hoy ───────────────────────────────────
        long ocupadasHoy = todasReservas.stream()
                .filter(r -> r.getFecha().equals(hoy))
                .filter(r -> r.getEstado() != Reserva.Estado.CANCELADA)
                .map(r -> r.getPista().getId())
                .distinct()
                .count();
        long disponiblesHoy = todasPistas.size() - ocupadasHoy;
        statDisponiblesNum.setText(String.valueOf(disponiblesHoy));
        statDisponiblesSub.setText(ocupadasHoy + " ocupadas");

        // ── Stat 3: Franja horaria más reservada ──────────────────────────────
        todasReservas.stream()
                .filter(r -> r.getEstado() != Reserva.Estado.CANCELADA)
                .collect(Collectors.groupingBy(
                        r -> r.getHoraInicio().getHour() + "h / " + r.getHoraFin().getHour() + "h",
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresentOrElse(
                        entry -> statHorarioNum.setText(entry.getKey()),
                        () -> statHorarioNum.setText("—")
                );
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

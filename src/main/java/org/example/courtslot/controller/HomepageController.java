package org.example.courtslot.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @FXML private Label     userNameLabel;
    @FXML private Button    adminBtn;
    @FXML private TextField searchField;
    @FXML private VBox      adminSidebar;
    @FXML private FlowPane  cardsContainer;

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

    // ── Rellena las cards de deporte ──────────────────────────────────────────

    private void cargarDeportes(String filtroNombre) {
        cardsContainer.getChildren().clear();
        List<Deporte> deportes = deporteService.getAll();

        for (Deporte deporte : deportes) {
            // Si hay filtro activo, saltamos los deportes que no coincidan
            if (filtroNombre != null && !filtroNombre.isBlank()) {
                if (!deporte.getNombre().toLowerCase().contains(filtroNombre.toLowerCase())) {
                    continue;
                }
            }
            VBox card = crearCardDeporte(deporte);
            cardsContainer.getChildren().add(card);
        }

        if (cardsContainer.getChildren().isEmpty()) {
            Label aviso = new Label("No se encontraron deportes.");
            cardsContainer.getChildren().add(aviso);
        }
    }

    // ── Construye la card visual de un deporte ────────────────────────────────

    private VBox crearCardDeporte(Deporte deporte) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefWidth(230);
        card.setMaxWidth(230);
        card.setMinWidth(200);

        // Imagen del deporte
        ImageView imagen = new ImageView();
        imagen.setFitWidth(230);
        imagen.setFitHeight(160);
        imagen.setPreserveRatio(false);
        cargarImagenDeporte(imagen, deporte);

        // Cuerpo de la card
        VBox cuerpo = new VBox(6);
        cuerpo.getStyleClass().add("card-body");

        Label lblNombre = new Label(deporte.getNombre());
        lblNombre.getStyleClass().add("card-title");
        cuerpo.getChildren().add(lblNombre);

        if (deporte.getDescripcion() != null && !deporte.getDescripcion().isBlank()) {
            Label lblDesc = new Label(deporte.getDescripcion());
            lblDesc.getStyleClass().add("card-subtitle");
            lblDesc.setWrapText(true);
            cuerpo.getChildren().add(lblDesc);
        }

        // Precio mínimo (lo calcula el service)
        Pista pistaMasBarata = pistaService.getPistaMasBarata(deporte.getId());
        if (pistaMasBarata != null) {
            Label lblPrecio = new Label(String.format("%.2f €/h", pistaMasBarata.getPrecioPorHora()));
            lblPrecio.getStyleClass().add("card-price");
            cuerpo.getChildren().add(lblPrecio);
        }

        // Botón reservar
        Button btnReservar = new Button("Reservar");
        btnReservar.getStyleClass().add("btn-reservar");
        btnReservar.setMaxWidth(Double.MAX_VALUE);
        btnReservar.setOnAction(e -> onClickReservar(deporte));
        cuerpo.getChildren().add(btnReservar);

        card.getChildren().addAll(imagen, cuerpo);
        return card;
    }

    // ── Carga la imagen del deporte por su campo icono o por nombre ───────────

    private void cargarImagenDeporte(ImageView iv, Deporte deporte) {
        // Primero intentamos con el campo icono guardado en la BD
        if (deporte.getIcono() != null && !deporte.getIcono().isBlank()) {
            URL urlImagen = HelloApplication.class.getResource("images/" + deporte.getIcono());
            if (urlImagen != null) {
                iv.setImage(new Image(urlImagen.toString()));
                return;
            }
        }
        // Si no hay icono, construimos el nombre del fichero a partir del nombre del deporte
        // Ejemplo: "Fútbol Sala" → "futbol-sala.png"
        String nombreFichero = deporte.getNombre().toLowerCase()
                .replace(" ", "-")
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u") + ".png";

        URL urlImagen = HelloApplication.class.getResource("images/" + nombreFichero);
        if (urlImagen != null) {
            iv.setImage(new Image(urlImagen.toString()));
        }
    }

    // ── Al pulsar "Reservar" en una card ──────────────────────────────────────

    private void onClickReservar(Deporte deporte) {
        if (!SessionManager.getInstance().estaLogueado()) {
            NavigationUtil.navigateTo("login.fxml", cardsContainer);
            return;
        }

        List<Pista> pistasDelDeporte = pistaService.getByDeporte(deporte.getId());
        if (pistasDelDeporte.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                    "No hay pistas disponibles para " + deporte.getNombre() + " en este momento.",
                    ButtonType.OK);
            alerta.setTitle("Sin pistas disponibles");
            alerta.showAndWait();
            return;
        }

        // Navegar a la pantalla de reserva y pasarle el deporte seleccionado
        BookingController ctrl = NavigationUtil.navigateToAndGetController("booking.fxml", cardsContainer);
        if (ctrl != null) {
            ctrl.setDeporte(deporte);
        }
    }

    // ── Buscador ──────────────────────────────────────────────────────────────

    @FXML
    protected void onBuscar() {
        cargarDeportes(searchField.getText().trim());
    }

    // ── Estadísticas admin ────────────────────────────────────────────────────

    private void cargarEstadisticasAdmin() {
        List<Pista> todasPistas = pistaService.getAll();
        LocalDate hoy = LocalDate.now();

        int reservadasHoy  = reservaService.contarReservasEnFecha(hoy);
        int disponiblesHoy = todasPistas.size() - reservadasHoy;
        if (disponiblesHoy < 0) disponiblesHoy = 0;

        statReservadasNum.setText(String.valueOf(reservadasHoy));
        statReservadasSub.setText("de " + todasPistas.size() + " pistas totales");
        statDisponiblesNum.setText(String.valueOf(disponiblesHoy));
        statDisponiblesSub.setText(reservadasHoy + " ocupadas");
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
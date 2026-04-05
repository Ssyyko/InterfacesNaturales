package com.mycompany.interfacesnaturales;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Resultado de aprendizaje a revisar:
 * interfaces naturales e informes con PDF y graficas.
 */
public class InterfacesNaturalesApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final Map<String, Integer> contadores = new LinkedHashMap<>();
    private final ObservableList<String> eventos = FXCollections.observableArrayList();
    private final ObservableList<PieChart.Data> datosTarta = FXCollections.observableArrayList();
    private final VBox panelContadores = new VBox(8);
    private final Path carpetaSalidas = Path.of("salidas");
    private final Path rutaBarras = carpetaSalidas.resolve("eventos_barras.jpg");
    private final Path rutaCircular = carpetaSalidas.resolve("eventos_circular.jpg");
    private final Path rutaPdf = carpetaSalidas.resolve("informe_interfaces_naturales.pdf");

    @Override
    public void start(Stage stage) {
        crearCarpeta();
        inicializarContadores();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef4f7, #dfe8ee);");

        Label titulo = new Label("Interfaces naturales");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #16324f;");

        Button btnPdf = new Button("Exportar PDF");
        btnPdf.setOnAction(e -> exportarPdf());

        HBox cabecera = new HBox(12, titulo, btnPdf);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        root.setTop(cabecera);

        StackPane zonaEventos = crearZonaInteractiva();
        VBox.setVgrow(zonaEventos, Priority.ALWAYS);

        PieChart grafico = new PieChart(datosTarta);
        grafico.setLegendVisible(false);
        grafico.setLabelsVisible(true);
        grafico.setTitle("Resumen rapido");
        grafico.setPrefHeight(250);

        panelContadores.setPadding(new Insets(10));
        panelContadores.setStyle("-fx-background-color: white; -fx-border-color: #bfcbd4;");
        refrescarPanelContadores();

        VBox derecha = new VBox(10, panelContadores, grafico);
        derecha.setPrefWidth(270);

        ListView<String> listaEventos = new ListView<>(eventos);
        listaEventos.setPrefHeight(180);

        VBox centro = new VBox(10, zonaEventos, new Label("Registro de eventos"), listaEventos);
        VBox.setVgrow(listaEventos, Priority.SOMETIMES);

        root.setCenter(centro);
        root.setRight(derecha);

        Scene scene = new Scene(root, 1080, 720);
        registrarEventosTeclado(scene);

        stage.setTitle("InterfacesNaturales");
        stage.setScene(scene);
        stage.show();
        zonaEventos.requestFocus();
    }

    private StackPane crearZonaInteractiva() {
        Rectangle fondo = new Rectangle(760, 360);
        fondo.setArcWidth(30);
        fondo.setArcHeight(30);
        fondo.setFill(Color.web("#8ecae6"));
        fondo.setStroke(Color.web("#1d3557"));
        fondo.setStrokeWidth(3);

        Label texto = new Label("Pulsa, arrastra, mueve la rueda,\nusa teclado o prueba gestos si tu equipo los soporta");
        texto.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #102a43;");

        StackPane zona = new StackPane(fondo, texto);
        zona.setPadding(new Insets(12));
        zona.setFocusTraversable(true);
        zona.setStyle("-fx-background-color: white; -fx-border-color: #aab7c4; -fx-border-width: 1;");

        zona.setOnMouseClicked(e -> registrar("Mouse clicked"));
        zona.setOnMousePressed(e -> registrar("Mouse pressed"));
        zona.setOnMouseReleased(e -> registrar("Mouse released"));
        zona.setOnMouseEntered(e -> registrar("Mouse entered"));
        zona.setOnMouseExited(e -> registrar("Mouse exited"));
        zona.setOnDragDetected(e -> registrar("Drag detected"));
        zona.setOnScroll(e -> registrar("Scroll"));
        zona.setOnZoom(e -> registrar("Zoom"));
        zona.setOnRotate(e -> registrar("Rotate"));
        zona.setOnSwipeUp(e -> registrar("Swipe up"));
        zona.setOnSwipeDown(e -> registrar("Swipe down"));
        zona.setOnSwipeLeft(e -> registrar("Swipe left"));
        zona.setOnSwipeRight(e -> registrar("Swipe right"));
        zona.setOnTouchPressed(e -> registrar("Touch pressed"));
        zona.setOnTouchMoved(e -> registrar("Touch moved"));
        zona.setOnTouchReleased(e -> registrar("Touch released"));
        return zona;
    }

    private void registrarEventosTeclado(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> registrar("Key pressed"));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> registrar("Key released"));
        scene.addEventHandler(KeyEvent.KEY_TYPED, e -> registrar("Key typed"));
    }

    private void inicializarContadores() {
        String[] tipos = {
            "Mouse clicked", "Mouse pressed", "Mouse released",
            "Mouse entered", "Mouse exited", "Drag detected", "Scroll", "Zoom", "Rotate",
            "Swipe up", "Swipe down", "Swipe left", "Swipe right",
            "Touch pressed", "Touch moved", "Touch released",
            "Key pressed", "Key released", "Key typed"
        };
        for (String tipo : tipos) {
            contadores.put(tipo, 0);
        }
    }

    private void registrar(String tipo) {
        contadores.put(tipo, contadores.get(tipo) + 1);
        eventos.add(0, tipo + " detectado");
        if (eventos.size() > 30) {
            eventos.remove(eventos.size() - 1);
        }
        refrescarPanelContadores();
    }

    private void refrescarPanelContadores() {
        panelContadores.getChildren().clear();
        datosTarta.clear();
        int mostrados = 0;
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            if (entry.getValue() > 0) {
                panelContadores.getChildren().add(new Label(entry.getKey() + ": " + entry.getValue()));
                datosTarta.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                mostrados++;
            }
        }
        if (mostrados == 0) {
            panelContadores.getChildren().add(new Label("Todavia no se ha registrado ningun evento."));
        }
    }

    private void crearCarpeta() {
        try {
            Files.createDirectories(carpetaSalidas);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de salidas", e);
        }
    }

    private void exportarPdf() {
        try {
            crearGraficas();
            crearDocumentoPdf();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void crearGraficas() throws IOException {
        DefaultCategoryDataset barras = new DefaultCategoryDataset();
        DefaultPieDataset<String> circular = new DefaultPieDataset<>();

        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            if (entry.getValue() > 0) {
                barras.addValue(entry.getValue(), "Eventos", entry.getKey());
                circular.setValue(entry.getKey(), entry.getValue());
            }
        }

        if (barras.getColumnCount() == 0) {
            barras.addValue(1, "Eventos", "Sin uso");
            circular.setValue("Sin uso", 1);
        }

        JFreeChart graficaBarras = ChartFactory.createBarChart(
                "Eventos registrados",
                "Tipo",
                "Cantidad",
                barras,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        ChartUtils.saveChartAsJPEG(rutaBarras.toFile(), graficaBarras, 900, 450);

        JFreeChart graficaCircular = ChartFactory.createPieChart(
                "Distribucion de eventos",
                circular,
                true,
                true,
                false
        );
        ChartUtils.saveChartAsJPEG(rutaCircular.toFile(), graficaCircular, 700, 420);
    }

    private void crearDocumentoPdf() throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            PDImageXObject imagenBarras = PDImageXObject.createFromFile(rutaBarras.toString(), documento);
            PDImageXObject imagenCircular = PDImageXObject.createFromFile(rutaCircular.toString(), documento);
            PDType1Font titulo = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font texto = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream stream = new PDPageContentStream(documento, pagina)) {
                stream.beginText();
                stream.setFont(titulo, 18);
                stream.newLineAtOffset(40, 790);
                stream.showText("Informe de interfaces naturales");
                stream.endText();

                stream.beginText();
                stream.setFont(texto, 11);
                stream.newLineAtOffset(40, 760);
                stream.showText("RA a revisar: interfaces naturales e informes.");
                int escritos = 0;
                for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
                    if (entry.getValue() > 0 && escritos < 8) {
                        stream.newLineAtOffset(0, -16);
                        stream.showText("- " + entry.getKey() + ": " + entry.getValue());
                        escritos++;
                    }
                }
                stream.endText();

                stream.drawImage(imagenBarras, 40, 320, 520, 230);
                stream.drawImage(imagenCircular, 150, 90, 300, 180);
            }

            documento.save(rutaPdf.toFile());
        }
    }
}

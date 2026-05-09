package com.sitmypet.controllers;

import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private PieChart chartRoles;
    @FXML private BarChart<String, Number> chartStatut;

    private ServiceUser serviceUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        List<User> users = serviceUser.afficher();
        
        int admin = 0, proprietaire = 0, gardien = 0;
        int actifs = 0, inactifs = 0;
        
        for (User u : users) {
             String role = u.getRole() != null ? u.getRole() : "";
             if (role.contains("ADMIN")) admin++;
             else if (role.contains("PROPRIETAIRE")) proprietaire++;
             else if (role.contains("GARDIEN")) gardien++;
             
             if (u.isActive()) actifs++;
             else inactifs++;
        }
        
        // Charger le graphique en camembert (Rôles)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Admins (" + admin + ")", admin),
            new PieChart.Data("Propriétaires (" + proprietaire + ")", proprietaire),
            new PieChart.Data("Gardiens (" + gardien + ")", gardien)
        );
        chartRoles.setData(pieData);
        
        // Charger le graphique en barres (Taux d'activité)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Actifs", actifs));
        series.getData().add(new XYChart.Data<>("Inactifs", inactifs));
        
        chartStatut.getData().add(series);
        
        // Styliser dynamiquement le BarChart pour correspondre au thème
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getXValue().equals("Actifs")) {
                data.getNode().setStyle("-fx-bar-fill: #2ecc71;"); // Vert
            } else {
                data.getNode().setStyle("-fx-bar-fill: #e74c3c;"); // Rouge
            }
        }
    }

    private void navigateToAdminShell(ActionEvent event, String section) throws IOException {
        AfficherUserController.INITIAL_ADMIN_SECTION = section != null ? section : "users";
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/AfficherUser.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setScene(new Scene(root, bounds.getWidth() * 0.9, bounds.getHeight() * 0.9));
        stage.setMaximized(true);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void handleAfficherUsers(ActionEvent event) throws IOException {
        navigateToAdminShell(event, "users");
    }

    @FXML
    private void handleNavAnimaux(ActionEvent event) throws IOException {
        navigateToAdminShell(event, "animaux");
    }

    @FXML
    private void handleNavAnnonces(ActionEvent event) throws IOException {
        navigateToAdminShell(event, "annonces");
    }

    @FXML
    private void handleNavPostulations(ActionEvent event) throws IOException {
        navigateToAdminShell(event, "postulations");
    }

    @FXML
    private void handleNavReclamations(ActionEvent event) throws IOException {
        navigateToAdminShell(event, "reclamations");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("SitMyPet - Connexion");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, Math.min(bounds.getWidth() * 0.9, 900), Math.min(bounds.getHeight() * 0.9, 600)));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

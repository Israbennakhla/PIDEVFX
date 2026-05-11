package com.sitmypet.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.*;
import java.util.HashMap;
import java.util.Map;

import com.sitmypet.model.EventParticipant;
import com.sitmypet.model.User;
import com.sitmypet.model.Evenement;
import com.sitmypet.services.ServiceEventParticipant;
import com.sitmypet.services.ServiceUser;
import com.sitmypet.services.ServiceEvenement;

public class ParticipantController {
    @FXML private TextField tfEventId;
    @FXML private TextField tfUserId;
    
    @FXML private TextField tfSearch;
    @FXML private ComboBox<Evenement> cbFilterEvent;
    @FXML private ComboBox<String> cbSort;
    
    @FXML private ListView<EventParticipant> listParticipants;
    
    private final ServiceEventParticipant service = new ServiceEventParticipant();
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceEvenement serviceEvenement = new ServiceEvenement();

    private final Map<Integer, User> userMap = new HashMap<>();
    private final Map<Integer, Evenement> eventMap = new HashMap<>();
    private final ObservableList<EventParticipant> participantsList = FXCollections.observableArrayList();
    private FilteredList<EventParticipant> filteredData;

    @FXML
    public void initialize() {
        for (User u : serviceUser.getAll()) userMap.put(u.getId(), u);
        for (Evenement e : serviceEvenement.getAll()) eventMap.put(e.getId(), e);

        // Map Evenement dummy value acting as "All Events"
        if (cbFilterEvent != null) {
            Evenement allEventsDummy = new Evenement(0, "Tous les événements", null, "", "", "");
            cbFilterEvent.getItems().add(allEventsDummy);
            cbFilterEvent.getItems().addAll(serviceEvenement.getAll());
            cbFilterEvent.getSelectionModel().selectFirst();
        }

        if (cbSort != null) {
            cbSort.getItems().addAll("Utilisateur (A-Z)", "Utilisateur (Z-A)", "Événement (A-Z)", "Événement (Z-A)");
            cbSort.getSelectionModel().selectFirst();
        }

        listParticipants.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EventParticipant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    User u = userMap.get(item.getUserId());
                    Evenement e = eventMap.get(item.getEventId());
                    
                    String userName = (u != null) ? u.getPrenom() + " " + u.getNom() : "ID " + item.getUserId();
                    String eventName = (e != null) ? e.getName() : "Event ID " + item.getEventId();
                    
                    setText(" " + userName + "   |    " + eventName);
                }
            }
        });

        filteredData = new FilteredList<>(participantsList, b -> true);

        Runnable refreshFilterAndSort = () -> {
            Evenement selEv = (cbFilterEvent != null) ? cbFilterEvent.getSelectionModel().getSelectedItem() : null;
            String query = (tfSearch != null && tfSearch.getText() != null) ? tfSearch.getText().toLowerCase() : "";

            filteredData.setPredicate(ep -> {
                // Event combo logic Filter
                if (selEv != null && selEv.getId() != 0) {
                    if (ep.getEventId() != selEv.getId()) return false;
                }
                
                // Text Search logic filter
                if (!query.isEmpty()) {
                    User u = userMap.get(ep.getUserId());
                    Evenement e = eventMap.get(ep.getEventId());
                    String userName = (u != null) ? (u.getPrenom() + " " + u.getNom()).toLowerCase() : "";
                    String eventName = (e != null) ? e.getName().toLowerCase() : "";
                    if (!userName.contains(query) && !eventName.contains(query)) return false;
                }
                return true;
            });
        };

        if (tfSearch != null) tfSearch.textProperty().addListener((obs, o, n) -> refreshFilterAndSort.run());
        if (cbFilterEvent != null) cbFilterEvent.valueProperty().addListener((obs, o, n) -> refreshFilterAndSort.run());

        SortedList<EventParticipant> sortedData = new SortedList<>(filteredData);
        if (cbSort != null) {
            cbSort.valueProperty().addListener((obs, o, sortOpt) -> applySort(sortedData, sortOpt));
            applySort(sortedData, cbSort.getValue());
        }

        listParticipants.setItems(sortedData);
        afficherParticipants();
    }
    
    private void applySort(SortedList<EventParticipant> sortedData, String sortOpt) {
        if (sortOpt == null) return;
        sortedData.setComparator((ep1, ep2) -> {
            User u1 = userMap.get(ep1.getUserId());
            User u2 = userMap.get(ep2.getUserId());
            String n1 = (u1 != null) ? (u1.getPrenom() + " " + u1.getNom()).toLowerCase() : "";
            String n2 = (u2 != null) ? (u2.getPrenom() + " " + u2.getNom()).toLowerCase() : "";

            Evenement e1 = eventMap.get(ep1.getEventId());
            Evenement e2 = eventMap.get(ep2.getEventId());
            String en1 = (e1 != null) ? e1.getName().toLowerCase() : "";
            String en2 = (e2 != null) ? e2.getName().toLowerCase() : "";

            switch (sortOpt) {
                case "Utilisateur (Z-A)": return n2.compareTo(n1);
                case "Événement (A-Z)": return en1.compareTo(en2);
                case "Événement (Z-A)": return en2.compareTo(en1);
                case "Utilisateur (A-Z)":
                default: return n1.compareTo(n2);
            }
        });
    }

    private void afficherParticipants() {
        participantsList.setAll(service.getAll());
    }

    @FXML
    public void ajouterParticipant(ActionEvent event) {
        if (!tfEventId.getText().isEmpty() && !tfUserId.getText().isEmpty()) {
            try {
                EventParticipant ep = new EventParticipant(
                    Integer.parseInt(tfEventId.getText()), 
                    Integer.parseInt(tfUserId.getText())
                );
                service.add(ep);
                afficherParticipants();
                tfEventId.clear();
                tfUserId.clear();
            } catch (NumberFormatException e) {
                System.out.println("ID doit être un entier !");
            }
        }
    }

    @FXML
    public void supprimerParticipant(ActionEvent event) {
        EventParticipant participantSelectionne = listParticipants.getSelectionModel().getSelectedItem();
        if (participantSelectionne != null) {
            service.delete(participantSelectionne);
            afficherParticipants();
        }
    }

    @FXML
    public void retourEvenements(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sitmypet/fxml/EvenementView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("PI-DEV : Gestion des Événements");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


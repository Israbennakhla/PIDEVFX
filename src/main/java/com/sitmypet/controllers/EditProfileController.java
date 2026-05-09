package com.sitmypet.controllers;

import com.sitmypet.exceptions.AuthenticationException;
import com.sitmypet.model.User;
import com.sitmypet.services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class EditProfileController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    
    @FXML private PasswordField txtAncienMotDePasse;
    @FXML private PasswordField txtNouveauMotDePasse;
    @FXML private PasswordField txtConfirmMotDePasse;
    
    @FXML private Label lblErreur;
    @FXML private Label lblSucces;

    private User currentUser;
    private ServiceUser serviceUser;
    private BorderPane mainContainer;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
    }

    public void initData(User user, BorderPane container) {
        this.currentUser = user;
        this.mainContainer = container;
        
        if (user != null) {
            txtNom.setText(user.getNom());
            txtPrenom.setText(user.getPrenom());
            txtEmail.setText(user.getEmail());
            txtTelephone.setText(user.getTelephone() != null ? user.getTelephone() : "");
            txtAdresse.setText(user.getAdresse() != null ? user.getAdresse() : "");
        }
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        lblErreur.setVisible(false);
        lblSucces.setVisible(false);

        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        
        String ancienMdp = txtAncienMotDePasse.getText();
        String nouveauMdp = txtNouveauMotDePasse.getText();
        String confirmMdp = txtConfirmMotDePasse.getText();

        // Validations de base
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs obligatoires (*).");
            return;
        }

        if (!telephone.matches("^\\d{8}$")) {
            afficherErreur("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        // Si l'utilisateur veut changer de mot de passe
        boolean mdpAChanger = !nouveauMdp.isEmpty() || !confirmMdp.isEmpty() || !ancienMdp.isEmpty();
        
        if (mdpAChanger) {
            if (ancienMdp.isEmpty() || nouveauMdp.isEmpty() || confirmMdp.isEmpty()) {
                afficherErreur("Pour changer de mot de passe, vous devez remplir les 3 champs de sécurité.");
                return;
            }
            if (!nouveauMdp.equals(confirmMdp)) {
                afficherErreur("Le nouveau mot de passe et sa confirmation ne correspondent pas.");
                return;
            }
            if (nouveauMdp.length() < 6) {
                afficherErreur("Le nouveau mot de passe doit faire au moins 6 caractères.");
                return;
            }

            try {
                // Vérifier l'ancien mot de passe
                serviceUser.authentifier(currentUser.getEmail(), ancienMdp);
                
                // Si pas d'exception, le mot de passe est bon
                boolean mdpModifie = serviceUser.changerMotDePasse(currentUser.getEmail(), nouveauMdp);
                if (!mdpModifie) {
                    afficherErreur("Erreur lors de la mise à jour du mot de passe.");
                    return;
                }
            } catch (AuthenticationException e) {
                afficherErreur("L'ancien mot de passe est incorrect.");
                return;
            }
        }

        // Mise à jour de l'objet utilisateur
        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setEmail(email);
        currentUser.setTelephone(telephone);
        currentUser.setAdresse(adresse);

        // Sauvegarde en BD
        serviceUser.modifier(currentUser);

        afficherSucces("Profil mis à jour avec succès !");
        
        // Vider les champs mot de passe pour la sécurité
        txtAncienMotDePasse.clear();
        txtNouveauMotDePasse.clear();
        txtConfirmMotDePasse.clear();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        if (mainContainer != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/UserProfile.fxml"));
                Parent view = loader.load();
                UserProfileController controller = loader.getController();
                controller.setUser(currentUser, mainContainer);
                mainContainer.setCenter(view);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void afficherErreur(String msg) {
        lblErreur.setText(msg);
        lblErreur.setVisible(true);
    }
    
    private void afficherSucces(String msg) {
        lblSucces.setText(msg);
        lblSucces.setVisible(true);
    }
}

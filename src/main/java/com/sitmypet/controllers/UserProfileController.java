package com.sitmypet.controllers;

import com.sitmypet.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.sitmypet.services.ServiceUser;

public class UserProfileController {

    @FXML private ImageView imgCover;
    @FXML private ImageView imgAvatar;
    @FXML private Label lblFullName;
    @FXML private Label lblRole;
    
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblAddress;
    @FXML private Label lblJoinDate;
    @FXML private Label lblStatus;

    private User currentUser;
    private ServiceUser serviceUser;
    private BorderPane mainContainer;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        // Appliquer un masque circulaire à l'avatar pour un style LinkedIn/Facebook
        Circle clip = new Circle(75, 75, 75); // Rayon de 75px
        imgAvatar.setClip(clip);
    }

    public void setUser(User user, BorderPane container) {
        this.currentUser = user;
        this.mainContainer = container;
        if (user != null) {
            lblFullName.setText(user.getPrenom() + " " + user.getNom());
            
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "Non défini";
            lblRole.setText(role);
            
            lblEmail.setText(user.getEmail());
            lblPhone.setText(user.getTelephone() != null ? user.getTelephone() : "Non renseigné");
            lblAddress.setText(user.getAdresse() != null ? user.getAdresse() : "Non renseignée");
            
            if (user.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                lblJoinDate.setText("Membre depuis le " + user.getCreatedAt().format(formatter));
            } else {
                lblJoinDate.setText("Membre de SitMyPet");
            }

            if (user.isActive()) {
                lblStatus.setText("Compte Actif");
                lblStatus.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-background-color: #e8f8f5; -fx-padding: 5 10; -fx-background-radius: 15;");
            } else {
                lblStatus.setText("Compte Inactif");
                lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-color: #fdedec; -fx-padding: 5 10; -fx-background-radius: 15;");
            }

            // Gestion de la photo de profil
            if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.png")) {
                try {
                    String photoUrl = user.getPhoto();
                    if (photoUrl.startsWith("http")) {
                        // Image depuis Google ou URL externe
                        imgAvatar.setImage(new Image(photoUrl, true));
                    } else {
                        // Image locale
                        File file = new File("src/main/resources/uploads/profiles/" + photoUrl);
                        if (!file.exists()) {
                            file = new File("SitMyPet-Desktop/src/main/resources/uploads/profiles/" + photoUrl);
                        }
                        if (file.exists()) {
                            imgAvatar.setImage(new Image(file.toURI().toString()));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Erreur chargement photo: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleChangePhoto(ActionEvent event) {
        if (currentUser == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Créer le dossier de destination si inexistant
                File uploadDir = new File("SitMyPet-Desktop/src/main/resources/uploads/profiles");
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    uploadDir = new File("src/main/resources/uploads/profiles");
                    uploadDir.mkdirs();
                }
                
                // Nouveau nom de fichier
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + extension;
                File destFile = new File(uploadDir, newFileName);
                
                // Copie du fichier
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Mise à jour de l'utilisateur
                currentUser.setPhoto(newFileName);
                serviceUser.modifier(currentUser);
                
                // Mettre à jour l'affichage
                imgAvatar.setImage(new Image(destFile.toURI().toString()));
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'upload de la photo : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEditProfile() {
        if (mainContainer != null && currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sitmypet/fxml/EditProfile.fxml"));
                Parent view = loader.load();
                EditProfileController controller = loader.getController();
                controller.initData(currentUser, mainContainer);
                mainContainer.setCenter(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

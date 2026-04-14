package com.sitmypet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FrontAccueilController {
    
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Bienvenue sur l'interface Front (Gardien / Propriétaire)");
    }
}

package com.sitmypet.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.util.Map;

public class CloudinaryService {

    private static CloudinaryService instance;
    private final Cloudinary cloudinary;

    private CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "ddkaoung0",
                "api_key",    "496361222669452",
                "api_secret", "TGfjgX3yEGMtF3pgq2Bv8EToSf0"
        ));
    }

    public static CloudinaryService getInstance() {
        if (instance == null) instance = new CloudinaryService();
        return instance;
    }

    /**
     * Upload une image vers Cloudinary
     * @param file fichier image local
     * @return URL publique de l'image
     */
    public String uploadImage(File file) {
        try {
            Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "sitmypet/reclamations",
                    "resource_type", "image"
            ));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload Cloudinary : " + e.getMessage());
        }
    }

    /**
     * Supprimer une image de Cloudinary via son URL
     */
    public void deleteImage(String imageUrl) {
        try {
            // Extraire le public_id depuis l'URL
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Erreur suppression Cloudinary : " + e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        // URL format: .../sitmypet/reclamations/filename.jpg
        String withoutExtension = url.substring(0, url.lastIndexOf('.'));
        return withoutExtension.substring(withoutExtension.indexOf("sitmypet"));
    }
}
package com.kapil.springecom.service;

import com.kapil.springecom.model.Product;
import com.kapil.springecom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    public List<Product> getAllProducts() {
        List<Product> products = productRepo.findAll();
        return mergeSortByPrice(products);
    }

    private List<Product> mergeSortByPrice(List<Product> products) {
        if (products.size() <= 1) {
            return products;
        }

        int mid = products.size() / 2;
        List<Product> left = mergeSortByPrice(new ArrayList<>(products.subList(0, mid)));
        List<Product> right = mergeSortByPrice(new ArrayList<>(products.subList(mid, products.size())));

        return mergeByPrice(left, right);
    }

    private List<Product> mergeByPrice(List<Product> left, List<Product> right) {
        List<Product> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getPrice().compareTo(right.get(j).getPrice()) <= 0)
            {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).orElse(new Product(-1));
    }
    public Product addOrUpdateProduct(Product incomingProduct, MultipartFile image) throws IOException {
        Product productToSave;

        // Agar ID diya gaya hai, toh try karo database se existing product lene ka
        if (incomingProduct.getId() != 0) {
            productToSave = productRepo.findById(incomingProduct.getId()).orElse(null);
        } else {
            productToSave = null;
        }

        // Agar existing product nahi mila, toh naya product banao
        if (productToSave == null) {
            productToSave = new Product();
        }

        // Har case me values update karo
        productToSave.setId(incomingProduct.getId()); // optional, for clarity
        productToSave.setName(incomingProduct.getName());
        productToSave.setDescription(incomingProduct.getDescription());
        productToSave.setBrand(incomingProduct.getBrand());
        productToSave.setPrice(incomingProduct.getPrice());
        productToSave.setCategory(incomingProduct.getCategory());
        productToSave.setReleaseDate(incomingProduct.getReleaseDate());
        productToSave.setProductAvailable(incomingProduct.isProductAvailable());
        productToSave.setStockQuantity(incomingProduct.getStockQuantity());

        // Image agar bheji gayi hai, toh update karo
        if (image != null && !image.isEmpty()) {
            productToSave.setImageName(image.getOriginalFilename());
            productToSave.setImageType(image.getContentType());
            productToSave.setImageData(image.getBytes());
        }

        // Save karo DB me
        return productRepo.save(productToSave);
    }



    public void deleteProduct(int id) {
        productRepo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }
}

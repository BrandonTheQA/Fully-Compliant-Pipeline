package com.example.repository;

import com.example.model.Product;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final Map<String, Product> products = new HashMap<>();

    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public boolean existsById(String id) {
        return products.containsKey(id);
    }

    public boolean existsByName(String name) {
        return products.values().stream().anyMatch(product -> product.getName().equals(name));
    }

    public long count() { return products.size(); }

    public void deleteById(String id) { products.remove(id); }
}



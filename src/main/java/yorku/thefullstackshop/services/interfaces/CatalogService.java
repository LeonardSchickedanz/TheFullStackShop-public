package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;

import java.util.List;
import java.util.Optional;

public interface CatalogService {
    List<Product> getProducts(String search, Integer brandId, Integer categoryId, String sort);

    Optional<Product> getProductById(int id);

    List<Brand> getBrands();

    List<Category> getCategories();
}
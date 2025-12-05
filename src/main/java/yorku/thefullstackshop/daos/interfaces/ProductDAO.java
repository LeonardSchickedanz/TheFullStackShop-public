package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;

import java.util.List;

public interface ProductDAO extends BaseDAO<Product> {
    List<Product> findWithFilters(String searchQuery, Integer brandId, Integer categoryId, String sortOrder);

    List<Brand> findAllBrands();

    List<Category> findAllCategories();

    int updateQuantity(int productId, int newQuantity);
}
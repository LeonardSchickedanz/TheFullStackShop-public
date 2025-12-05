package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.interfaces.ProductDAO;
import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.services.interfaces.CatalogService;

import java.util.List;
import java.util.Optional;

public class CatalogServiceImpl implements CatalogService {

    private final ProductDAO productDAO;

    public CatalogServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public CatalogServiceImpl() {
        this.productDAO = new ProductDAOImpl();
    }

    @Override
    public List<Product> getProducts(String search, Integer brandId, Integer categoryId, String sort) {
        return productDAO.findWithFilters(search, brandId, categoryId, sort);
    }

    @Override
    public Optional<Product> getProductById(int id) {
        return productDAO.findById(id);
    }

    @Override
    public List<Brand> getBrands() {
        return productDAO.findAllBrands();
    }

    @Override
    public List<Category> getCategories() {
        return productDAO.findAllCategories();
    }
}
package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.interfaces.ProductDAO;
import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.services.implementations.CatalogServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Test
    void testGetProducts_WithFilters() {
        String search = "laptop";
        Integer brandId = 1;
        Integer categoryId = 2;
        String sort = "price_desc";
        List<Product> expectedProducts = Arrays.asList(new Product(), new Product());

        when(productDAO.findWithFilters(search, brandId, categoryId, sort)).thenReturn(expectedProducts);

        List<Product> actualProducts = catalogService.getProducts(search, brandId, categoryId, sort);

        assertEquals(2, actualProducts.size());
        verify(productDAO, times(1)).findWithFilters(search, brandId, categoryId, sort);
    }

    @Test
    void testGetProductById_Found() {
        int productId = 5;
        Product expectedProduct = new Product();
        when(productDAO.findById(productId)).thenReturn(Optional.of(expectedProduct));

        Optional<Product> result = catalogService.getProductById(productId);

        assertTrue(result.isPresent());
        assertEquals(expectedProduct, result.get());
        verify(productDAO).findById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        int productId = 99;
        when(productDAO.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> result = catalogService.getProductById(productId);

        assertFalse(result.isPresent());
        verify(productDAO).findById(productId);
    }

    @Test
    void testGetBrands() {
        List<Brand> expectedBrands = Arrays.asList(new Brand(), new Brand(), new Brand());
        when(productDAO.findAllBrands()).thenReturn(expectedBrands);

        List<Brand> result = catalogService.getBrands();

        assertEquals(3, result.size());
        verify(productDAO).findAllBrands();
    }

    @Test
    void testGetCategories() {
        List<Category> expectedCategories = Arrays.asList(new Category(), new Category());
        when(productDAO.findAllCategories()).thenReturn(expectedCategories);

        List<Category> result = catalogService.getCategories();

        assertEquals(2, result.size());
        verify(productDAO).findAllCategories();
    }
}
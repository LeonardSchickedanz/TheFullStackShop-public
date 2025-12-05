package yorku.thefullstackshop.apis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import yorku.thefullstackshop.models.CartItem;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import java.util.Optional;

@Path("/shoppingcart")
public class ShoppingCartApi {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartApi(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    public ShoppingCartApi() {
        this.shoppingCartService = new ShoppingCartServiceImpl();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(@FormParam("productId") int productId, @FormParam("quantity") int quantity, @Context HttpServletRequest request) {

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getUserId() : null;
        String sessionId = session.getId();

        if (quantity < 1) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Quantity must be at least 1\"}").build();
        }

        try {
            shoppingCartService.addItemToCart(userId, sessionId, productId, quantity);
            int newCount = shoppingCartService.getCartItemCount(userId, sessionId);
            return Response.ok("{\"count\": " + newCount + "}").build();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Not enough")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Not enough stock available.\"}").build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Error adding to cart\"}").build();
        }
    }

    @GET
    @Path("/product/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductCartStatus(@PathParam("id") int productId, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getUserId() : null;
        String sessionId = session.getId();

        ShoppingCart cart = shoppingCartService.getCart(userId, sessionId);
        int inCart = 0;

        if (cart != null && cart.getCartItems() != null) {
            Optional<CartItem> item = cart.getCartItems().stream().filter(i -> i.getProduct().getProductId() == productId).findFirst();
            if (item.isPresent()) {
                inCart = item.get().getQuantity();
            }
        }
        return Response.ok("{\"inCart\": " + inCart + "}").build();
    }
}
package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("cart")
@CrossOrigin
public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping("")
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // use the shoppingcartDao to get all items in the cart and return the cart
            // retrieve the shopping cart for the user
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve shopping cart: " + e);
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{id}")
    @PreAuthorize("isAuthenticated()" )
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addToCart(Principal principal, @PathVariable int id)
    {
        try
        {
            // get the logged in users username
            String userName = principal.getName();

            // look up the user in the database
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // add the product to the cart
            // this should insert if product isn't in the cart and increment
            shoppingCartDao.addToCart(id, userId);

            // return the updated cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart: " + e);
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("/products/{id}")
    public ShoppingCart updateQuantity(Principal principal, @PathVariable int id, @RequestBody ShoppingCartItem item) {

        try
        {
            // get the username
            String userName = principal.getName();

            // get the user
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // update the quantity for the product in the cart
            shoppingCartDao.editCart(id, userId, item.getQuantity());

            // return the updated cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update cart item quantity: " + e);
        }
    }


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping("")
    public ShoppingCart clearCart(Principal principal)
    {
        try
        {
            // get username
            String userName = principal.getName();

            // loop up the user
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // delete all the items for the user
            shoppingCartDao.clearCart(userId);

            // return an empty cart object
            return new ShoppingCart();
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to clear shopping cart" + e);
        }
    }
}

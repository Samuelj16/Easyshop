package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.UserDao;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("orders")
public class OrderController {
    private final OrderDao orderDAO;
    private final UserDao userDao;

    public OrderController(OrderDao orderDAO, UserDao userDao) {
        this.orderDAO = orderDAO;
        this.userDao = userDao;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public void checkout(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            int userId = user.getId();
            orderDAO.checkout(userId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}

package com.powerfit.powerfit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavegacionController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/clases")
    public String clases() {
        return "reservaClases";
    }

    @GetMapping("/tienda")
    public String tienda() {
        return "tienda";
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }

    @GetMapping("/perfil")
    public String perfil() {
        return "perfilUsuario";
    }
}
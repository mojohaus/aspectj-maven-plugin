package it;

import lombok.Getter;

@Getter
public class Greeter {

    private final String name = "world";

    public String greet() {
        return "Hello " + getName();
    }
}

package it;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class TracingAspect {

    @Before("execution(* it.Greeter.greet())")
    public void trace() {
        System.out.println("greet called");
    }
}

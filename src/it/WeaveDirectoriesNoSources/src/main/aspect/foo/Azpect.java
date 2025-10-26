package foo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class Azpect {
    @Before("execution(* foo.Clazz.doSomething(..))")
    public void beforeDoSomething(JoinPoint joinPoint) {
        System.out.println("Before doSomething");
    }
}

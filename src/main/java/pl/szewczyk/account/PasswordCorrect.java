package pl.szewczyk.account;

import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordCorrectValidator.class)
@Documented
public @interface PasswordCorrect {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

@Component
class PasswordCorrectValidator implements ConstraintValidator<PasswordCorrect, Object> {



    public PasswordCorrectValidator() {

    }

    @Override
    public void initialize(PasswordCorrect passwordCorrect) {

    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        UserForm account = (UserForm) o;
        return account.getPassword().equals(account.getRepeatPassword());
    }


}

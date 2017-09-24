package pl.szewczyk.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.logging.Logger;

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
    @Autowired
    private final AccountRepository accountRepository;

    Logger logger = Logger.getLogger(PasswordCorrectValidator.class.getName());

    public PasswordCorrectValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void initialize(PasswordCorrect passwordCorrect) {
        logger.severe("initialize");
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        logger.severe("valid?");
        UserForm account = (UserForm) o;
        if (account.getPassword().equals(account.getRepeatPassword())) {
            logger.severe("VALID");
            return true;
        }
        logger.severe("INVALID");
        return false;
    }


}

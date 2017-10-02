package pl.szewczyk.projects;

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
@Constraint(validatedBy = LikeCorrectValidator.class)
@Documented
public @interface LikeCorrect {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

@Component
class LikeCorrectValidator implements ConstraintValidator<LikeCorrect, Object> {

    Logger logger = Logger.getLogger(LikeCorrectValidator.class.getName());

    public LikeCorrectValidator() {

    }

    @Override
    public void initialize(LikeCorrect passwordCorrect) {
        logger.severe("initialize");
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        logger.severe("valid?");
        ProjectForm projectForm = (ProjectForm) o;
        if (projectForm.isLike()) {
            if (projectForm.getLikeFrequency() != null) {
                return true;
            }
        } else {
            return true;
        }
        logger.severe("INVALID");
        return false;
    }


}

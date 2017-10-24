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

    public LikeCorrectValidator() {

    }

    @Override
    public void initialize(LikeCorrect passwordCorrect) {

    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        System.out.println("valid?");
        ProjectForm projectForm = (ProjectForm) o;
        if (projectForm.getLike()) {
            if (projectForm.getLikeFrequency() != null) {
                return true;
            }
        } else {
            return true;
        }
        System.out.println("INVALID");
        return false;
    }


}

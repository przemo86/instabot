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
@Constraint(validatedBy = CommentCorrectValidator.class)
@Documented
public @interface CommentCorrect {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

@Component
class CommentCorrectValidator implements ConstraintValidator<CommentCorrect, Object> {

    Logger logger = Logger.getLogger(CommentCorrectValidator.class.getName());

    public CommentCorrectValidator() {

    }

    @Override
    public void initialize(CommentCorrect passwordCorrect) {
        logger.severe("initialize");
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {

        ProjectForm projectForm = (ProjectForm) o;
        if (projectForm.getComment() != null && projectForm.getComment()) {
            if (projectForm.getCommentFrequency() != null)
                if (projectForm.getCommentString() != null)
                    if (!projectForm.getCommentString().equals("")) {
                        System.out.println("COMMENT VALID - si commento");
                        return true;
                    }
        } else {
            System.out.println("COMMENT VALID - co nommento");
            return true;
        }
        System.out.println("INCVALID COMMENT");

        return false;
    }


}

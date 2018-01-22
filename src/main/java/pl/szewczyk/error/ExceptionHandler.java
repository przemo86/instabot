package pl.szewczyk.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.logging.Logger;

/**
 * General error handler for the application.
 */
@ControllerAdvice
class ExceptionHandler {
    protected Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Handle exceptions thrown by handlers.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    public ModelAndView exception(Exception exception, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.addObject("errorMessage", exception.getMessage());
        log.severe(exception.getMessage());
        exception.printStackTrace();
        return modelAndView;
    }
}
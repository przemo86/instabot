package pl.szewczyk.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletRegistration;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {ApplicationConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }
//
//    @Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//        super.onStartup(servletContext);
//        ServletRegistration.Dynamic appServlet = servletContext.addServlet("mvc", new DispatcherServlet(
//                new GenericWebApplicationContext()));
//
//        appServlet.setLoadOnStartup(1);
//
//        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getenv("TEMP"),
//                4096000, 4096000 * 2, 4096000 / 2);
//
//        appServlet.setMultipartConfig(multipartConfigElement);
//    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        DelegatingFilterProxy securityFilterChain = new DelegatingFilterProxy("springSecurityFilterChain");

        return new Filter[] {characterEncodingFilter, securityFilterChain};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("defaultHtmlEscape", "true");
        registration.setInitParameter("spring.profiles.active", "default");

    }


}
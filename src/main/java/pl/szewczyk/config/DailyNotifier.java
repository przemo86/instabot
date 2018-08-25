package pl.szewczyk.config;

import me.postaddict.instagram.scraper.interceptor.UserAgentInterceptor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;


public class DailyNotifier implements InterruptableJob {
    @Autowired
    private EntityManager em;

    private Thread currentThread;

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Override
    public void interrupt() {

        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    protected void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        if (em == null)
            init();

log.severe("EXECUTE");
        this.currentThread = Thread.currentThread();
log.severe("EXECUTE 2");

        String html = "";
try {
    String running = em.createNativeQuery("select count(*) from instabot.project where status = true").getSingleResult().toString();
    log.severe("RUNNUNG " + running);
    String stats = em.createNativeQuery("select count(*) from instabot.statistics where time > current_date - interval '1 day'").getSingleResult().toString();
    log.severe("stats " + stats);
    List<Object[]> list = em.createNativeQuery("select name, (select count(*) from instabot.statistics where projectid = p.id) from instabot.project p order by name").getResultList();
    log.severe("list " + list);

        html = "\t<p>Runnig projects # <span style=\"font-weight: bolder\">"+running+"</span></p>\n" +
                "\t<p>Runnig projects #\t<span style=\"font-weight: bolder\">"+stats+"</span></p>\n" +
                "\t<p><h5>Runnig projects</h5>\n";
    html += "<table>\n" +
            "\t<tr>\n" +
            "\t\t<th style=\"padding: 3px;\"><span>Nazwa</span></th><th><span>Ilość statystyk</span></p></th>\n" +
            "\t\t</tr>";

        for (Object[] o : list) {
            html += "<tr><td><span style=\"padding: 3px;\">"+o[0].toString()+"</span></td><td><span style=\"padding: 3px;\">"+o[1].toString()+"</span></td></tr>";
        }
        html += "</table>";
    log.severe("HRML " + html);

} catch (Exception e) {
    log.severe("ERROR " + e.getMessage());
}
        FormBody formBody = new FormBody.Builder()
                .add("from", "instabot@instabot.pl")
                .add("to", "przemyslaw.szewczyk+gun@gmail.com")
                .add("subject", "Daily Notification")
                .add("html", html)
                .build();
        Request request = new Request.Builder()
                .url("https://api.mailgun.net/v3/sandboxf47ae8a48f4646008368277d6ce2c46b.mailgun.org/messages")
                .header("Content-Type", "multipart/form-data")
                .header("Authorization", "Basic YXBpOjhlOWZjNWJlMTU0NTRkZTU1YWU0M2I1ZTU1NzJhNTdhLTg4ODkxMjdkLTYyZjU5ZGQ4")
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient().newBuilder()
                .build();

        try {
            log.severe("send");
            httpClient.newCall(request).execute();
            log.severe("done");
        } catch (IOException e) {
            log.severe("GONE WILD " + e.getMessage());
            e.printStackTrace();
        }
    }
}

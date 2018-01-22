package pl.szewczyk.instagram;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.domain.Account;
import me.postaddict.instagram.scraper.interceptor.UserAgentInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.account.InstaUserRepository;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

/**
 * Created by przem on 15.09.2017.
 */

//@SessionScope
public class InstaConstants {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    private DefaultCookieJar cookieJar = new DefaultCookieJar(new CookieHashSet());

    private Instagram instagramAnonymous;
    private Instagram instagramLoggedIn;
    private final String ALIAS = "scaleeffect";
    private PrivateKey myPrivateKey;
    private PublicKey myPublicKey;

    @Autowired
    private InstaUserRepository instaUserRepository;

    @PostConstruct
    private void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        initKeyStore();
    }

    public byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (myPublicKey == null)
            initKeyStore();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, myPublicKey);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (myPrivateKey == null)
            initKeyStore();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, myPrivateKey);
        return cipher.doFinal(ciphertext);
    }

    public Instagram getInstagramAnonymous() {
        instagramAnonymous = new Instagram(new OkHttpClient().newBuilder()
                .addInterceptor(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"))
                .build());
        return instagramAnonymous;
    }

    public Instagram getInstagramLoggedIn(String login) {
        if (instagramLoggedIn != null) {
            log.info("JEST ZALOGOWANY " + instagramLoggedIn.getUsername() + " A CHCE " + login);
            if (instagramLoggedIn.getUsername().equals(login)) {
                log.info("");
                return instagramLoggedIn;
            }
        }

        log.info("NO NIESTETY, MUSZE LOGOWAC...");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        cookieJar.removeCookie("sessionid");
        cookieJar.removeCookie("ds_user_id");
//        log.info("PRINT COOKLIE");
//        cookieJar.print();
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder()
                .addInterceptor(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"))
                .cookieJar(cookieJar).build());


        try {
            String pass = getUserPass(login);
            loginInstagram(login, pass);
            return instagramLoggedIn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserPass(String login) {
        if (instaUserRepository == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        }
        InstaUser user = instaUserRepository.findByUserName(login);
        byte[] password = user.getPassword();
        try {
            byte[] decPass = decrypt(password);
            String pass = new String(decPass, "UTF-8");
            log.info("LOGIN " + login + " with pass " + pass);
//            instagramLoggedIn.login1(user.getInstaUserName(), pass, 0);

            return pass;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loginInstagram(String login, String passwordd) throws Exception {
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder()
//                .addInterceptor(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"))
                .cookieJar(cookieJar).build());
        instagramLoggedIn.login1(login, passwordd, 0);
    }

    public Account instaLogin(String login, String passwordd) throws Exception {
        log.info("INSTALOGIN " + login);
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder()
               // .addInterceptor(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"))
                .cookieJar(cookieJar).build());
        cookieJar.removeCookie("sessionid");
        cookieJar.removeCookie("ds_user_id");
        try {
            return instagramLoggedIn.login(login, passwordd, 0);
        } catch (Exception e) {
            log.severe("ERROPr" + e);
            log.severe("ERROPr" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void initKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] password = new char[]{'m', '6', 'f', 'L', 'T', '8', 'g', 'k', 'a', 'x', 'D', 'p', '6', 'p', 'U', '2'};

            InputStream fis = null;
            try {
                fis = getClass().getClassLoader().getResourceAsStream("keystore.jks");
                ks.load(fis, password);


                KeyStore.ProtectionParameter protParam =
                        new KeyStore.PasswordProtection(password);

                KeyStore.PrivateKeyEntry privEntry = (KeyStore.PrivateKeyEntry)
                        ks.getEntry(ALIAS, protParam);

                myPrivateKey = privEntry.getPrivateKey();
                myPublicKey = ks.getCertificate(ALIAS).getPublicKey();

            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}

package pl.szewczyk.instagram;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.domain.Account;
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

/**
 * Created by przem on 15.09.2017.
 */

public class InstaConstants {

    private DefaultCookieJar cookieJar = new DefaultCookieJar(new CookieHashSet());

    private Instagram instagramAnonymous;
    private Instagram instagramLoggedIn;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private final String ALIAS = "scaleeffect";
    private PrivateKey myPrivateKey;
    private PublicKey myPublicKey;

    @Autowired
    private InstaUserRepository instaUserRepository;

    @PostConstruct
    private void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        instagramAnonymous = new Instagram(okHttpClient);
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder().cookieJar(cookieJar).build());
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
        instagramAnonymous = new Instagram(new OkHttpClient());
        return instagramAnonymous;
    }

    public Instagram getInstagramLoggedIn(String login) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        cookieJar.removeCookie("sessionid");
        cookieJar.removeCookie("ds_user_id");
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder().cookieJar(cookieJar).build());

        InstaUser user = instaUserRepository.findByUserName(login);
        byte[] password = user.getPassword();

        try {
            byte[] decPass = decrypt(password);
            String pass = new String(decPass, "UTF-8");
            instagramLoggedIn.login1(user.getInstaUserName(), pass);
            System.out.println("RET " + instagramLoggedIn);
            return instagramLoggedIn;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Account instaLogin(String login, String passwordd) throws Exception {
        System.out.println("INSTALOGIN " + login);
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder().cookieJar(cookieJar).build());
        cookieJar.removeCookie("sessionid");
        cookieJar.removeCookie("ds_user_id");
        return instagramLoggedIn.login(login, passwordd);

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

package pl.szewczyk.instagram;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.domain.Account;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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
import java.util.Collections;

/**
 * Created by przem on 15.09.2017.
 */
@Component
@Scope(value = "prototype")
public class InstaConstants {

    private CookieJar cookieJar = new DefaultCookieJar(new CookieHashSet());

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
        instagramAnonymous = new Instagram(okHttpClient);
        instagramLoggedIn = new Instagram(new OkHttpClient().newBuilder().cookieJar(cookieJar).build());

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] password = new char[]{'m', '6', 'f', 'L', 'T', '8', 'g', 'k', 'a', 'x', 'D', 'p', '6', 'p', 'U', '2'};

            InputStream fis = null;
            try {
                fis = this.getClass().getResourceAsStream("keystore.jks");
                ks.load(fis, password);
                System.out.println("asss " + password);
                System.out.println(" adad " + this.getClass().getResource("keystore.jks"));

                KeyStore.ProtectionParameter protParam =
                        new KeyStore.PasswordProtection(password);
                System.out.println("----------");
                Collections.list(ks.aliases()).stream().forEach(c -> System.out.println(c));
                System.out.println("----------");

                KeyStore.PrivateKeyEntry privEntry = (KeyStore.PrivateKeyEntry)
                        ks.getEntry(ALIAS, protParam);

                System.out.println("priv entry " + privEntry);
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
            }finally {
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

    private byte[] encrypt(PrivateKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    private byte[] decrypt(PublicKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }

    public Instagram getInstagramAnonymous() {
        return instagramAnonymous;
    }

    public Instagram getInstagramLoggedIn(String login) {
        InstaUser user = instaUserRepository.findByUserName(login);
        byte[] password = user.getPassword();

        try {
            byte[] decPass = decrypt(myPublicKey, password);
            String pass = new String(decPass, "UTF-8");

            instagramLoggedIn.login(login, pass);

            return instagramLoggedIn;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Account instaLogin(String login, String passwordd) {

        try {
            instagramLoggedIn.login(login, passwordd);

            return instagramLoggedIn.getAccountById(5860887512L);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

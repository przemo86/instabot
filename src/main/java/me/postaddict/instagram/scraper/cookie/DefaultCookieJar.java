package me.postaddict.instagram.scraper.cookie;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class DefaultCookieJar implements CookieJar {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    private CookieCache cache;

    public DefaultCookieJar(CookieCache cache) {
        this.cache = cache;
    }

    @Override
    synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
    }

    @Override
    synchronized public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie cookie = it.next();
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                it.remove();
            } else if (cookie.matches(url)) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    synchronized public void removeCookie(String name) {
        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie cookie = it.next();
            if (cookie.name().equals(name))
                it.remove();
        }
    }

}

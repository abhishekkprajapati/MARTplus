package com.ShopSphere.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DisposableEmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$"
    );

    @Value("${disposable.email.api.key:}")
    private String externalApiKey;

    @Value("${disposable.email.api.url:}")
    private String externalApiUrl;

    @Value("${email.dns.mx.check:true}")
    private boolean checkMxRecords;

    // Comprehensive blocklist of known disposable / temporary / throwaway email domains
    private static final Set<String> DISPOSABLE_DOMAINS = new HashSet<>(Arrays.asList(
            "mailinator.com", "mailinator2.com", "mailin8r.com", "10minutemail.com", "10minutemail.net",
            "10minutemail.co.uk", "10minutemail.de", "20minutemail.com", "30minutemail.com", "60minutemail.com",
            "tempmail.com", "temp-mail.org", "temp-mail.io", "tempmailaddress.com", "mytemp.email", "mytempmail.com",
            "guerrillamail.com", "guerrillamail.net", "guerrillamail.org", "guerrillamail.biz", "guerrillamail.de",
            "guerrillamailblock.com", "sharklasers.com", "grr.la", "pokemail.net", "spam4.me",
            "yopmail.com", "yopmail.net", "yopmail.fr", "cool.fr.nf", "jetable.fr.nf",
            "trashmail.com", "trashmail.net", "trashmail.org", "trashmail.at", "trashmail.de", "trashmail.me",
            "dispostable.com", "throwawaymail.com", "fakeinbox.com", "fakemailgenerator.com", "getairmail.com",
            "nada.ltd", "getnada.com", "mohmal.com", "crazymailing.com", "generator.email",
            "emailondeck.com", "burnermail.io", "maildrop.cc", "inboxkitten.com", "dropmail.me",
            "harakirimail.com", "mailcatch.com", "spambog.com", "tempinbox.com", "mailnesia.com",
            "mailnull.com", "e4ward.com", "gishpuppy.com", "sneakemail.com", "jetable.org",
            "kasmail.com", "mintemail.com", "zoemail.com", "meltmail.com", "filzmail.com",
            "trashymail.com", "deadaddress.com", "mailfreeonline.com", "mailexpire.com", "mytrashmail.com",
            "spamgourmet.com", "temporaryemail.net", "anonymbox.net", "binkmail.com", "bobmail.info",
            "chacuo.net", "devnullmail.com", "discard.email", "discardmail.com", "disposablemail.com",
            "dodgeit.com", "drdrb.net", "dumpmail.de", "emailgo.de", "emailigo.de",
            "ephemail.net", "frapmail.com", "hidemail.de", "instantmail.org", "klzlk.com",
            "kurzepost.de", "lookugly.com", "maildu.de", "mailimate.com", "mailincubator.com",
            "mailmetrash.com", "mailmoat.com", "mailshell.com", "mailslurp.com", "mailtemp.net",
            "mailtothis.com", "messagebeamer.de", "nobulk.com", "noclickemail.com", "nomail.xl.cx",
            "nospam.ze.tc", "nospam4.us", "notsharingmy.info", "nowmymail.com", "oneoffmail.com",
            "onewaymail.com", "otherinbox.com", "pookmail.com", "quickinbox.com", "rcpt.at",
            "safersignup.com", "safetymail.info", "shortmail.net", "sofort-mail.de", "sogetthis.com",
            "soodonims.com", "spamavert.com", "spambox.us", "spamdecoy.net", "spamex.com",
            "spamex.net", "spamfree24.org", "spamhole.com", "spaminator.de", "spaml.de",
            "spammotel.com", "spamspot.com", "superrito.com", "suremail.info", "tafmail.com",
            "teleworm.us", "temporaryinbox.com", "thespambox.com", "throwawayemailaddress.com", "tmail.ws",
            "trash-mail.at", "trash-mail.com", "trash-mail.de", "trash-me.com", "trashmailer.com",
            "tyldd.com", "uggsrock.com", "wegwerfadresse.de", "wegwerfemail.de", "wegwerfmail.de",
            "wegwerfmail.net", "wegwerfmail.org", "whyspam.me", "willhackforfood.biz", "willselfdestruct.com",
            "winmail.win", "wuzup.net", "zippymail.info", "zoemail.org", "0clickemail.com",
            "anonbox.net", "antichef.com", "antichef.net", "armyspy.com", "cuvox.de",
            "dayrep.com", "einrot.com", "fleckens.hu", "gustr.com", "jourrapide.com", "rhyta.com"
    ));

    /**
     * Validates email syntax, checks against disposable domains, and optionally checks MX records.
     * Throws RuntimeException with clear error message if invalid.
     */
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        String normalized = email.trim().toLowerCase();

        // 1. Syntax Check
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        int atIndex = normalized.indexOf('@');
        if (atIndex <= 0 || atIndex == normalized.length() - 1) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        String domain = normalized.substring(atIndex + 1);

        // 2. Disposable Domain Blocklist Check
        if (isDisposableDomain(domain)) {
            throw new RuntimeException("Temporary or disposable email addresses are not allowed.");
        }

        // 3. Optional External Disposable API Check (if configured via env)
        if (externalApiKey != null && !externalApiKey.isBlank() && externalApiUrl != null && !externalApiUrl.isBlank()) {
            if (checkExternalDisposableApi(normalized, domain)) {
                throw new RuntimeException("Temporary or disposable email addresses are not allowed.");
            }
        }

        // 4. MX Record Validation (if enabled and domain has dots)
        if (checkMxRecords && !domain.equals("localhost") && !domain.endsWith(".test") && !domain.endsWith(".local") && !domain.equals("example.com") && !domain.endsWith(".example.com")) {
            if (!hasMxOrAddressRecord(domain)) {
                throw new RuntimeException("The email domain does not appear to be valid or able to receive mail.");
            }
        }
    }

    public boolean isDisposableDomain(String domain) {
        if (domain == null) return false;
        String cleanDomain = domain.toLowerCase().trim();

        if (DISPOSABLE_DOMAINS.contains(cleanDomain)) {
            return true;
        }

        // Check subdomain matching (e.g. sub.mailinator.com)
        for (String blocked : DISPOSABLE_DOMAINS) {
            if (cleanDomain.endsWith("." + blocked)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verify whether the domain has MX records in DNS via JNDI.
     */
    private boolean hasMxOrAddressRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns://8.8.8.8");
            env.put("com.sun.jndi.dns.timeout.initial", "2000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX", "A"});

            if (attrs == null) {
                return false;
            }

            Attribute attrMx = attrs.get("MX");
            if (attrMx != null && attrMx.size() > 0) {
                return true;
            }

            Attribute attrA = attrs.get("A");
            return attrA != null && attrA.size() > 0;
        } catch (NamingException e) {
            // If DNS lookup fails or host not found
            return false;
        } catch (Exception e) {
            // Fallback for isolated offline dev environments
            return true;
        }
    }

    /**
     * Optional external disposable email check via REST API.
     */
    private boolean checkExternalDisposableApi(String email, String domain) {
        try {
            String requestUrl = externalApiUrl.replace("{email}", email).replace("{domain}", domain);
            URL url = URI.create(requestUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + externalApiKey);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String res = response.toString().toLowerCase();
                    return res.contains("\"disposable\":true") || res.contains("\"is_disposable\":true") || res.contains("\"disposable\": true");
                }
            }
        } catch (Exception e) {
            // Do not crash if external API is unreachable; rely on local curated blocklist
            System.err.println("Notice: External disposable email API check failed: " + e.getMessage());
        }
        return false;
    }
}

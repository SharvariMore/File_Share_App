package in.sharvarimore.fileshareapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component                        // register class as bean

public class ClerkJwksProvider {

    // get JSON key set url
    @Value("${clerk.jwks-url}")   // reads and injects property values
    private String jwksUrl;

    private final Map<String, PublicKey> keyCache = new HashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_TTL = 3600000;  //1hr


    // Request method to valid JWT token from frontend
    public PublicKey getPublicKey(String kid) throws Exception {
        if (keyCache.containsKey(kid) && System.currentTimeMillis() - lastFetchTime < CACHE_TTL) {
            return keyCache.get(kid);  // key is already present in cache and not expired
        }

        refreshKeys();
        return keyCache.get(kid);
    }

    private void refreshKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper();           // convert JSON to Java object
        JsonNode jwks = mapper.readTree(new URL(jwksUrl).openStream());  // read url and pass it

        // JsonNode is individual element or key-value pairs
        JsonNode keys = jwks.get("keys");
        for(JsonNode keyNode: keys) {
            String kid = keyNode.get("kid").asString();
            String kty = keyNode.get("kty").asString();  // key type
            String alg = keyNode.get("alg").asString();  // algorithm

            if("RSA".equals(kty) && "RS256".equals(alg)) {
                String n = keyNode.get("n").asString();  // modulus
                String e = keyNode.get("e").asString();  // exponential

                PublicKey publicKey = createPublicKey(n, e);
                keyCache.put(kid, publicKey);
            }
        }
        lastFetchTime = System.currentTimeMillis();   // update last fetch time for publicKey
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);

        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
        BigInteger exponentBigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);       // return RSA public key

    }
}

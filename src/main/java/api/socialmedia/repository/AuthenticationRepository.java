package api.socialmedia.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * token storage
 */


@Repository
public class AuthenticationRepository {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();


    public void putTokenAndUsername(String token, String username) {
        tokens.put(token, username);
    }

    public String getUserNameByToken(String token) {
        return tokens.get(token);
    }

    public void removeTokenAndUsernameByToken(String token) {
        tokens.remove(token);
    }

    public String getTokenByUsername(String username) {
        Set<Map.Entry<String, String>> entrySet = tokens.entrySet();

        String token = "";
        for (Map.Entry<String, String> pair : entrySet) {
            if (username.equals(pair.getValue())) {
                token = pair.getKey();
            }
        }
        if (token.equals("")) {
            return null;
        }
        return token;
    }
}

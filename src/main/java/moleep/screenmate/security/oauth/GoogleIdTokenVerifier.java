package moleep.screenmate.security.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.config.GoogleOAuthProperties;
import moleep.screenmate.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

    private final GoogleOAuthProperties googleOAuthProperties;

    public GoogleUserInfo verify(String idTokenString) {
        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                    new Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(Collections.singletonList(googleOAuthProperties.getClientId()))
                            .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new UnauthorizedException("INVALID_GOOGLE_TOKEN", "Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            return GoogleUserInfo.builder()
                    .googleId(payload.getSubject())
                    .email(payload.getEmail())
                    .emailVerified(payload.getEmailVerified())
                    .displayName((String) payload.get("name"))
                    .profileImageUrl((String) payload.get("picture"))
                    .build();

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Google ID token", e);
            throw new UnauthorizedException("GOOGLE_VERIFICATION_FAILED", "Failed to verify Google ID token");
        }
    }
}

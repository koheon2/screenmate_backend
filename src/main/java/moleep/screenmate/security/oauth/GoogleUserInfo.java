package moleep.screenmate.security.oauth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleUserInfo {

    private final String googleId;
    private final String email;
    private final Boolean emailVerified;
    private final String displayName;
    private final String profileImageUrl;
}

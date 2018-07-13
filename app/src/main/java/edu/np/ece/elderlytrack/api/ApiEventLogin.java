package edu.np.ece.elderlytrack.api;

import edu.np.ece.elderlytrack.model.AuthToken;

public class ApiEventLogin extends ApiEvent {
    AuthToken authToken = null;

    public ApiEventLogin() {
    }

    public ApiEventLogin(AuthToken authToken) {
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }
}

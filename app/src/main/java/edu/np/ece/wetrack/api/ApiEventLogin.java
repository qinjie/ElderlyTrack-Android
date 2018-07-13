package edu.np.ece.wetrack.api;

import edu.np.ece.wetrack.model.AuthToken;

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

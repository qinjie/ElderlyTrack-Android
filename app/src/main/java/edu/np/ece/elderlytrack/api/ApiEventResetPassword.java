package edu.np.ece.elderlytrack.api;

import edu.np.ece.elderlytrack.model.AuthToken;

public class ApiEventResetPassword extends ApiEvent {
    AuthToken authToken = null;

    public ApiEventResetPassword() {
    }

    public ApiEventResetPassword(AuthToken authToken) {
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }
}

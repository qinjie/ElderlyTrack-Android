package edu.np.ece.wetrack.api;

import edu.np.ece.wetrack.model.AuthToken;

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

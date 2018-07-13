package edu.np.ece.wetrack.api;

import static edu.np.ece.wetrack.api.Constants.BASE_URL;

public class ApiClient {

    public static ApiInterface getApiInterface() {
        return RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
    }
}

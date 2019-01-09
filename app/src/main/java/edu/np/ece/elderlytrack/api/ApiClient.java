package edu.np.ece.elderlytrack.api;

import java.util.List;

import edu.np.ece.elderlytrack.model.AuthToken;
import edu.np.ece.elderlytrack.model.LocationWithBeacon;
import edu.np.ece.elderlytrack.model.Missing;
import edu.np.ece.elderlytrack.model.MissingWithResident;
import edu.np.ece.elderlytrack.model.NearbyItem;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;
import edu.np.ece.elderlytrack.model.Setting;

import static edu.np.ece.elderlytrack.api.Constants.BASE_URL;

public class ApiClient {

    public static ApiInterface getApiInterface() {
        return RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
    }

    public static class ApiEvent {
        protected boolean isResponded = false;
        protected boolean isSuccessful = false;
        protected int statusCode = 500;
        protected String message = null;

        public boolean isSuccessful() {
            return isSuccessful;
        }

        public void setSuccessful(boolean successful) {
            isSuccessful = successful;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public boolean isResponded() {
            return isResponded;
        }

        public void setResponded(boolean responded) {
            this.isResponded = responded;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ApiEventCloseMissing extends ApiEvent {
        List<Missing> closedMissings = null;

        public ApiEventCloseMissing() {
        }

        public ApiEventCloseMissing(List<Missing> closedMissings) {
            this.closedMissings = closedMissings;
        }

        public List<Missing> getClosedMissings() {
            return closedMissings;
        }

        public void setClosedMissings(List<Missing> closedMissings) {
            this.closedMissings = closedMissings;
        }
    }

    public static class ApiEventForgotPassword extends ApiEvent {
    }

    public static class ApiEventListBeaconsOfMissing extends ApiEvent {
        List<NearbyItem> missingBeacons = null;

        public ApiEventListBeaconsOfMissing() {
        }

        public ApiEventListBeaconsOfMissing(List<NearbyItem> missingBeacons) {
            this.missingBeacons = missingBeacons;
        }

        public List<NearbyItem> getMissingBeacons() {
            return missingBeacons;
        }

        public void setMissingBeacons(List<NearbyItem> missingBeacons) {
            this.missingBeacons = missingBeacons;
        }
    }

    public static class ApiEventListLocationByMissingId extends ApiEvent {
        List<LocationWithBeacon> locationsWithBeacon = null;

        public ApiEventListLocationByMissingId() {
        }

        public ApiEventListLocationByMissingId(List<LocationWithBeacon> locationsWithBeacon) {
            this.locationsWithBeacon = locationsWithBeacon;
        }

        public List<LocationWithBeacon> getLocationsWithBeacon() {
            return locationsWithBeacon;
        }

        public void setLocationsWithBeacon(List<LocationWithBeacon> locationsWithBeacon) {
            this.locationsWithBeacon = locationsWithBeacon;
        }
    }

    public static class ApiEventListMissingResidents extends ApiEvent {
        List<ResidentWithMissing> missingResidents = null;

        public ApiEventListMissingResidents() {
        }

        public ApiEventListMissingResidents(List<ResidentWithMissing> missingBeacons) {
            this.missingResidents = missingBeacons;
        }

        public List<ResidentWithMissing> getMissingResidents() {
            return missingResidents;
        }

        public void setMissingResidents(List<ResidentWithMissing> missingResidents) {
            this.missingResidents = missingResidents;
        }
    }

    public static class ApiEventListRelativeResidents extends ApiEvent {
        List<ResidentWithMissing> relativeResidents = null;

        public ApiEventListRelativeResidents() {
        }

        public ApiEventListRelativeResidents(List<ResidentWithMissing> missingBeacons) {
            this.relativeResidents = missingBeacons;
        }

        public List<ResidentWithMissing> getRelativeResidents() {
            return relativeResidents;
        }

        public void setRelativeResidents(List<ResidentWithMissing> relativeResidents) {
            this.relativeResidents = relativeResidents;
        }
    }

    public static class ApiEventListSettings extends ApiEvent {
        List<Setting> settings = null;

        public ApiEventListSettings() {
        }

        public ApiEventListSettings(List<Setting> settings) {
            this.settings = settings;
        }

        public List<Setting> listSettings() {
            return settings;
        }

        public void setSettings(List<Setting> settings) {
            this.settings = settings;
        }
    }

    public static class ApiEventLogin extends ApiEvent {
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

    public static class ApiEventReportMissing extends ApiEvent {
        MissingWithResident missingWithResident = null;

        public MissingWithResident getMissingWithResident() {
            return missingWithResident;
        }

        public void setMissingWithResident(MissingWithResident missingWithResident) {
            this.missingWithResident = missingWithResident;
        }
    }

    public static class ApiEventResetPassword extends ApiEvent {
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

    public static class ApiEventGetResident extends ApiEvent {

        public ApiEventGetResident() {
        }

        public ApiEventGetResident(ResidentWithMissing resident) {
            this.resident = resident;
        }

        ResidentWithMissing resident = null;

        public ResidentWithMissing getResident() {
            return resident;
        }

        public void setResident(ResidentWithMissing resident) {
            this.resident = resident;
        }
    }

}

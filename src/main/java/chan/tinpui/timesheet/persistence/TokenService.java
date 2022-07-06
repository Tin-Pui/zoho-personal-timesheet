package chan.tinpui.timesheet.persistence;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.crm.api.exception.SDKException;

import java.util.Optional;

public interface TokenService {
    Optional<OAuthToken> loadAuthToken() throws SDKException;
    void saveAuthToken(OAuthToken token) throws SDKException;
}

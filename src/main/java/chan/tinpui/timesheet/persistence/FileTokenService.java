package chan.tinpui.timesheet.persistence;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.FileStore;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.exception.SDKException;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class FileTokenService implements TokenService {

    private final TokenStore tokenStore;

    public FileTokenService(String fileStorePath) throws Exception {
        if (fileStorePath.contains(File.separator)) {
            Files.createDirectories(new File(fileStorePath.substring(0, fileStorePath.lastIndexOf(File.separatorChar))).toPath());
        }
        this.tokenStore = new FileStore(fileStorePath);
    }

    @Override
    public Optional<OAuthToken> loadAuthToken() throws SDKException {
        List<Token> tokens = tokenStore.getTokens();
        return tokens.isEmpty() ? Optional.empty() : Optional.of((OAuthToken) tokens.get(0));
    }

    @Override
    public void saveAuthToken(OAuthToken token) throws SDKException {
        tokenStore.deleteTokens();
        UserSignature userSignature;
        try {
            userSignature = new UserSignature(token.getUserMail());
        } catch (Exception e) {
            userSignature = new UserSignature("login.example@email.com");
        }
        tokenStore.saveToken(userSignature, token);
    }
}

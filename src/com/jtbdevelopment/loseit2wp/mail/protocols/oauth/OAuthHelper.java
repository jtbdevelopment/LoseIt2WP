package com.jtbdevelopment.loseit2wp.mail.protocols.oauth;

import java.io.UnsupportedEncodingException;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

//  Taken from
//  http://nilvec.com/implementing-smtp-or-imap-xoauth-authentication-in-java/
//  and reduced to used portions

//  TODO - eliminate this or oauth - have signpost + xoauth + oauth
public class OAuthHelper {

    public OAuthConsumer mConsumer;
    private OAuthProvider mProvider;

    private String mCallbackUrl;

    public OAuthHelper(String consumerKey, String consumerSecret,
                       String scope, String callbackUrl, String appname)
            throws UnsupportedEncodingException {
        String reqUrl;
        if (appname == null)
            reqUrl = OAuth.addQueryParameters(
                    "https://www.google.com/accounts/OAuthGetRequestToken",
                    "scope", scope);
        else
            reqUrl = OAuth.addQueryParameters(
                    "https://www.google.com/accounts/OAuthGetRequestToken",
                    "scope", scope, "xoauth_displayname", appname);

        mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);

        mProvider = new CommonsHttpOAuthProvider(reqUrl,
                "https://www.google.com/accounts/OAuthGetAccessToken",
                "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");
        mProvider.setOAuth10a(true);

        mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);
    }

    public String getRequestToken()
            throws OAuthMessageSignerException, OAuthNotAuthorizedException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        String authUrl =
                mProvider.retrieveRequestToken(mConsumer, mCallbackUrl);
        return authUrl;
    }

    public String[] getAccessToken(String verifier)
            throws OAuthMessageSignerException, OAuthNotAuthorizedException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        mProvider.retrieveAccessToken(mConsumer, verifier);
        return new String[] {
                mConsumer.getToken(), mConsumer.getTokenSecret()
        };
    }
}



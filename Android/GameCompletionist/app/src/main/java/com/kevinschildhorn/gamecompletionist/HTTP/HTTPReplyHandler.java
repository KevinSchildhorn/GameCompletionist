package com.kevinschildhorn.gamecompletionist.HTTP;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 05, 2014.
 *
 * HTTPReplyHandlers functions are called from the HTTPRequestHandler,
 * after a message is received from the Platforms API
 */

public interface HTTPReplyHandler {

    // Steam

    //  Present incoming Steam ID
    public void incomingSteamID(String steamID);
    // Present incoming List of Games
    public void incomingSteamGameList(JSONObject gameList) throws JSONException;
}
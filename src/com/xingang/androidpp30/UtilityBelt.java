package com.xingang.androidpp30;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.widget.Toast;

public class UtilityBelt {
	
	final static String EOL = System.getProperty("line.separator"); 


	/**
	 * Given either a Spannable String or a regular String and a token, apply
	 * the given CharacterStyle to the span between the tokens, and also
	 * remove tokens.
	 * <p>
	 * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
	 * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence
	 * {@code "Hello world!"} with {@code world} in red.
	 *
	 * @param text The text, with the tokens, to adjust.
	 * @param token The token string; there should be at least two instances
	 *             of token in text.
	 * @param cs The style to apply to the CharSequence. WARNING: You cannot
	 *            send the same two instances of this parameter, otherwise
	 *            the second call will remove the original span.
	 * @return A Spannable CharSequence with the new style applied.
	 *
	 * @see http://developer.android.com/reference/android/text/style/CharacterStyle.html
	 */
	public static CharSequence setSpanBetweenTokens(CharSequence text,
	    String token, CharacterStyle... cs)
	{
	    // Start and end refer to the points where the span will apply
	    int tokenLen = token.length();
	    int start = text.toString().indexOf(token) + tokenLen;
	    int end = text.toString().indexOf(token, start);

	    if (start > -1 && end > -1)
	    {
	        // Copy the spannable string to a mutable spannable string
	        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
	        for (CharacterStyle c : cs)
	            ssb.setSpan(c, start, end, 0);

	        // Delete the tokens before and after the span
	        ssb.delete(end, end + tokenLen);
	        ssb.delete(start - tokenLen, start);

	        text = ssb;
	    }

	    return text;
	}
	
    //  Validate user inputs and push build XML string to be passed to SIP server
    public static String	buildSipConfigXml ( Context context )  {
    	// Load Persistent data from Advanced and Settings tabs
        SharedPreferences sipTab = context.getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
 
        if (sipTab.getBoolean("sipEnabled", false) == false ) {
        	// SIP is not enabled so do not build XML
        	// this is not an error condition
        	return null;
        }
        
        // Before we start to build the XML lets validate a few fields
        if ( sipTab.getString("proxyIPEdit", "").length() == 0 ) {
        	// Proxy IP/HOST is a required field 
			Toast.makeText(context, 
				       R.string.proxyRequired, 
				       Toast.LENGTH_LONG).show();
        	return null;
        }
        if ( sipTab.getString("userNameEdit", "").length() == 0 ) {
        	// User Name is a required field
			Toast.makeText(context, 
				       R.string.userNameRequired, 
				       Toast.LENGTH_LONG).show();
        	return null;
        }

        
        // Convert Reference File Spinner Index into file type    
        int refFileInt = sipTab.getInt("referenceFile", 0);  
        String refFileStr = "pp30-2.5";
        switch (refFileInt ) {
        case 1:
        	refFileStr = "pp30-2.2";
        	break;
        case 2:
        	refFileStr = "pp30-2.3";
        	break;
        case 3:
        	refFileStr = "pp30-2.6";
        	break;        	
        default:
        	refFileStr = "pp30-2.5";
        }

        // If Registrar is blank then use Proxy
        String regIP = sipTab.getString("registrarIPEdit", "");
        if (regIP.length() == 0 ) {
        	regIP = sipTab.getString("proxyIPEdit", "");
        }
        
        // If Domain is blank then use Proxy
        String domain = sipTab.getString("domainEdit", "");
        if ( domain.length() == 0 ) {
        	domain = sipTab.getString("proxyIPEdit", "");
        }

        
        // If Auth User name is blank then use User name
        String authUserName = sipTab.getString("authUserNameEdit", "");
        if (authUserName.length() == 0 ) {
        	authUserName = sipTab.getString("userNameEdit", "");
        }
        
        // Convert Record Route Spinner Index into a string
        String recRoute = "strict";
        if ( sipTab.getInt("recordRoutingSpin", 0) == 1 ) {
        	recRoute = "loose";
        }
        
    	StringBuffer	sXmlStr = new StringBuffer ( );
    	
    	sXmlStr.append ( "<?xml version=\"1.0\"?>" + EOL );
    	sXmlStr.append ( "<sipresponder-config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + EOL );
    	sXmlStr.append ( "  <sip>" + EOL);
    	sXmlStr.append ( "	<medias>" + EOL );
    	sXmlStr.append ( "		<port-range>" + EOL );
    	sXmlStr.append ( "			<port-min>" + sipTab.getString("minMediaPort", "5000") + "</port-min>" + EOL );
    	sXmlStr.append ( "			<port-max>" + sipTab.getString("maxMediaPort", "13000") + "</port-max>" + EOL );
    	sXmlStr.append ( "		</port-range>" + EOL );
    	sXmlStr.append ( "		<codec-config>" + EOL );
    	sXmlStr.append ( "			<default-codec-type>" + sipTab.getString("codecEdit", "AMR-WB") + "</default-codec-type>" + EOL );
    	sXmlStr.append ( "			<amr-payload-type>" + sipTab.getString("amrPayloadEdit", "102") + "</amr-payload-type>" + EOL );
    	sXmlStr.append ( "			<amr-wb-payload-type>" + sipTab.getString("amrWbPayloadEdit", "104") + "</amr-wb-payload-type>" + EOL );
    	sXmlStr.append ( "		</codec-config>" + EOL );
    	sXmlStr.append ( "	</medias>" + EOL );
    	sXmlStr.append ( "    <endpoints>" + EOL);
    	sXmlStr.append ( "      <endpoint id=\"1\">" + EOL);
    	sXmlStr.append ( "        <outbound-interface>default</outbound-interface>" + EOL);
    	sXmlStr.append ( "        <ip-address>");
    	sXmlStr.append ( getLocalIpAddress());
    	sXmlStr.append ("</ip-address>" + EOL);
    	sXmlStr.append ("        <port>" + sipTab.getString("outboundPort", "5060") + "</port>" + EOL);
    	sXmlStr.append ("        <always-include-port>" + Boolean.toString(sipTab.getBoolean("alwaysIncludePort", false)) +"</always-include-port>" + EOL);
    	sXmlStr.append ("        <always-send-ip-address>true</always-send-ip-address>" + EOL);
    	sXmlStr.append ("        <include-rport>" + Boolean.toString(sipTab.getBoolean("includeRPort", true)) + "</include-rport>" + EOL);
    	sXmlStr.append ("        <type-of-service>" + sipTab.getString("tos", "01100000") + "</type-of-service>" + EOL);
    	sXmlStr.append ("        <session-timer>3600</session-timer>" + EOL);
    	sXmlStr.append ("        <expires-timer>" + sipTab.getString("regInt", "3600") + "</expires-timer>" + EOL);
    	sXmlStr.append ("        <user>" + EOL);
    	sXmlStr.append ("          <display-name>" + sipTab.getString("displayNameEdit", "USER") + "</display-name>" + EOL);
    	sXmlStr.append ("          <username>" + sipTab.getString("userNameEdit", "") + "</username>" + EOL);
    	sXmlStr.append ("          <domain>" + domain + "</domain>" + EOL);
    	sXmlStr.append ("          <trust-identity>none</trust-identity>" + EOL);
    	sXmlStr.append ("          <credentials>" + EOL);
    	sXmlStr.append ("            <credential>" + EOL);
    	sXmlStr.append ("              <realm>" + sipTab.getString("realmEdit", "") + "</realm>" + EOL);
    	sXmlStr.append ("              <username>" + authUserName + "</username>" + EOL);
    	sXmlStr.append ("              <password>" + sipTab.getString("authPasswordEdit", "") + "</password>" + EOL);
    	sXmlStr.append ("            </credential>" + EOL);
    	sXmlStr.append ("          </credentials>" + EOL);
    	sXmlStr.append ("        <proxies>" + EOL);
    	sXmlStr.append ("		  <proxy>" + EOL);
    	sXmlStr.append ("			<record-routing>" + recRoute + "</record-routing>" + EOL);
    	sXmlStr.append ("          	<address>" + sipTab.getString("proxyIPEdit", "") + "</address>" + EOL);
    	sXmlStr.append ("         	<port>" + sipTab.getString("proxyPortEdit", "5060") + "</port>" + EOL);
    	sXmlStr.append ("		  </proxy>" + EOL);
    	
    	// Add Proxy #2 if it exists
    	if ( sipTab.getString("proxyIPEdit1", "").length() >= 1 ) {
        	sXmlStr.append ("		  <proxy>" + EOL);
        	sXmlStr.append ("			<record-routing>" + recRoute + "</record-routing>" + EOL);
        	sXmlStr.append ("			<address>" + sipTab.getString("proxyIPEdit1", "") + "</address>" + EOL);
        	sXmlStr.append ("			<port>" + sipTab.getString("proxyPortEdit1", "5060") + "</port>" + EOL);
        	sXmlStr.append ("		  </proxy>" + EOL);   		
    	}
    	
    	// Add Proxy #3 if it exists
    	if ( sipTab.getString("proxyIPEdit2", "").length() >= 1 ) {
        	sXmlStr.append ("		  <proxy>" + EOL);
        	sXmlStr.append ("			<record-routing>" + recRoute + "</record-routing>" + EOL);
        	sXmlStr.append ("			<address>" + sipTab.getString("proxyIPEdit2", "") + "</address>" + EOL);
        	sXmlStr.append ("			<port>" + sipTab.getString("proxyPortEdit2", "5060") + "</port>" + EOL);
        	sXmlStr.append ("		  </proxy>" + EOL);   		
    	}    	
    	
    	sXmlStr.append ("		</proxies>" + EOL);
    	sXmlStr.append ("          <registrar>" + EOL);
    	sXmlStr.append ("            <address>" + regIP + "</address>" + EOL);
    	sXmlStr.append ("            <port>" + sipTab.getString("registrarPortEdit", "5060") + "</port>" + EOL);
    	sXmlStr.append ( "          </registrar>" + EOL);
    	sXmlStr.append ("        </user>" + EOL);

    	sXmlStr.append ("		<termination-emulation>" + refFileStr + "</termination-emulation>" + EOL);
    	sXmlStr.append ("      </endpoint>" + EOL);
    	sXmlStr.append ("    </endpoints>" + EOL);
    	sXmlStr.append ("  </sip>" + EOL);
    	sXmlStr.append ("</sipresponder-config>" + EOL);

    	return new String ( sXmlStr );
    }
    
  
    public static boolean isLicensed(Context context ) {
		// to turn off Temp License just return true here
    	//return true;
    	
    	// Temp License is based on a date
    	// Update date to whenever Temp license should expire
    	// or set it to 40 years from now to simulate never expire
        int year = 2012;
        int month = 8;
        int day = 30;

        String date = year + "/" + month + "/" + day;
        java.util.Date licDate = null;

        try {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
          licDate = formatter.parse(date);
          Date today = new Date();
          if ( today.before(licDate) == true ) {
        	  return true;
          }
        } catch (ParseException e) {
          System.out.println(e.toString());
          e.printStackTrace();
        }
		Toast.makeText(context, 
			       R.string.invalid_license, 
			       Toast.LENGTH_LONG).show();   	
		return false;
	}
    
	public static String getTimeStampStr( Date d) {
		if ( d == null ) {
			d = new Date();
		}
	    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault());	
		return  new String ( sdf.format(d) ); 
	}
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        //Log.e(LOG_TAG, ex.toString());
	    }
	    return null;
	}
}

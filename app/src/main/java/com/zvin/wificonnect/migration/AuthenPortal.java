package com.zvin.wificonnect.migration;

import android.util.Xml;

import com.zvin.wificonnect.util.Const;
import com.zvin.wificonnect.util.LogUtil;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhengwen on 2015/12/28.
 */
public class AuthenPortal {
    private static final String TAG = "AuthenPortal--->";
    private PortalResponseObj unknownCode = new PortalResponseObj(-1, null, null);
    private PortalResponseObj responseObj = unknownCode;
    private String preLoginResponse;
    private String preCookie;

    private final String KEYWORD_CMCCCS = "cmcccs";
    private final String KEYWORD_LOGINREQ = "login_req";
    private final String KEYWORD_LOGINRES = "login_res";
    private final String KEYWORD_OFFLINERES = "offline_res";
    private final String SEPARATOR = "|";
    private final String KEYWORD_PWDRES = "applypwd_res";
    private final String PREFIX_HTTPS = "https";
    private final String URL_PREFIX2 = "var url";
    private final String INDICATOR_REDIRECT_PORTALURL = "portalurl";
    private String INDICATOR_LOGIN_AC_NAME = "wlanacname";
    private String INDICATOR_LOGIN_USER_IP = "wlanuserip";
    private String INDICATOR_LOGIN_AC_IP = "wlanacip";
    private String INDICATOR_LOGIN_USERNAME = "USER";
    private String INDICATOR_LOGIN_PASSWORD = "PWD";
    private String INDICATOR_LOGIN_CLIENTTYPE = "clienttype";
    private String LOGINURL = "LoginURL";
    private String INDICATOR_LOGIN_ACTIONTYPE = "actiontype";
    private final String VALUE_REFRESH = "refresh";
    private final String URL_PREFIX1 = "URL=";
    private final String ATTR_HTTP_EQUIV = "http-equiv";
    private final String ATTR_CONTENT = "content";

    //预登陆参数
    private Map<String, String> connParams_perLogin = new HashMap<String, String>();
    //下线参数
    private Map<String, String> connParams_logout = new HashMap<String, String>();

    private final String CMCC_LOGINFORM_NAME = "loginform";
    private String wlanServiceUrl;
    private Boolean isCancelLogin = false;

    private PortalUrl pu = new PortalUrl();
    public String URL = null;

    /**
     *
     * 预登录，获取引导页
     *
     * @return int -1 网络异常 0 已登录 1 正常，集团环境 2 正常，广东环境 3 正常，漫游环境 4 未知环境
     *
     */
    public int preLogin(PortalHttp http, String judgeRoaming) {
        String location = null;
        String requestUrl = Const.BAIDU_URL;
        try {
            LogUtil.i(TAG, "1-1发起百度请求");
            long time1 = System.currentTimeMillis();
            location = http.sendGetOneStep(Const.BAIDU_URL);
            String url = Const.BAIDU_URL;
            if (location != null) {
                // 根据location判断漫游
                if(judgeRoaming != null) {
                    String response302 = http.getResponse302();
                    LogUtil.i(TAG, "AuthenPortal judgeRoaming: " + judgeRoaming);

                    String judgeRoamings[] = judgeRoaming.split(";");
                    for (int i = 0; i < judgeRoamings.length; i++) {
                        if (location.indexOf(judgeRoamings[i]) != -1) {
                            // is roaming?
                            return 3;
                        }
                        if (response302 != null && response302.indexOf(judgeRoamings[i]) != -1) {
                            // is roaming by response302
                            return 3;
                        }
                    }
                }
                // 不满足漫游条件，下一步继续跳转
                if (location.equals(Const.BAIDU_URL)) {
                    // 200, baidu or portalurl or other, 直接解析页面
                    String guideHtml = http.getResponse();
                    if (guideHtml != null) {
                        return checkFirstPage(http, guideHtml,judgeRoaming);
                    } else {
                        // 200,但Response为null,服务器数据异常
                        responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_ALL, null, requestUrl);
                    }
                } else {
                    // normal portal, 需要进一步跳转
                    url = location;
                    LogUtil.i(TAG, "1-2获得认证平台地址（AC 302跳转）location=" + url);
                    LogUtil.i(TAG, "1发起百度请求到获得认证平台地址  Time="
                            + (System.currentTimeMillis() - time1) + "ms");
                }
                requestUrl = url;
                // 302 location继续跳转
                LogUtil.i(TAG, "2-1访问认证平台");
                long time2 = System.currentTimeMillis();
                PortalHttp httpStep2 = new PortalHttp();
                if (httpStep2.sendDataGet(false, url)) {
                    String guideHtml = httpStep2.getResponse();
                    if (guideHtml != null) {
                        LogUtil.i(TAG, "2-2得到认证平台页面（login_req）");
                        LogUtil.i(TAG, "2访问认证平台，得到认证平台页面  Time="
                                + (System.currentTimeMillis() - time2) + "ms");
                        LogUtil.i(TAG, "guideHtml   " + "guideHtml:" + guideHtml);
                        return checkFirstPage(httpStep2, guideHtml,judgeRoaming);
                    } else {
                        // 200,但Response为null,服务器数据异常
                        responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_ALL,
                                null, requestUrl);
                    }
                }
            } else {
                // 302,但Location为null,服务器数据异常
                responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_ALL, null, requestUrl);
            }
        } catch (CMCCWifiAuthException e) {
            // CMCCWifiAuthException to PortalResponseObj
            responseObj = new PortalResponseObj(e);
            return -1;
        }
        return 4;
    }

    /**
     *
     * 解析第一个页面
     *
     * @return int 0 已登录 1 正常，集团环境 2 正常，广东环境 3 正常，漫游环境 4 未知环境
     *
     */
    private  int checkFirstPage(PortalHttp http, String pageHtml,String judgeRoaming) {
        if (pageHtml.indexOf("cmcccs|login_req") != -1
                || pageHtml.indexOf("AppUA") != -1) {

            // Portal页面，解析参数
            // 这一步，解析基本不会失败
            processRedirectData(pageHtml, http.getCookie());
            if (pageHtml.indexOf(".jsp") >= 0||pageHtml.indexOf("gd") >= 0) {
                return 2;
            } else {
                return 1;
            }
        } else {
            if (checkIsLoginned(http.getHost(), pageHtml)) {
                return 0;
            }
            if(judgeRoaming!=null){
                LogUtil.i(TAG, "AuthenPortal judgeRoaming: " + judgeRoaming);
                String judgeRoamings[] = judgeRoaming.split(";");
                for (int i = 0; i < judgeRoamings.length; i++) {
                    if (pageHtml.indexOf(judgeRoamings[i]) != -1) {
                        // is roaming?
                        return 3;
                    }
                }
            }


            // 可能是portalurl页面，需要再次跳转后解析参数
            if (processRedirectData(pageHtml, http.getCookie())) {
                // 解析正常，统一返回1集团环境（此处不区分广东环境和集团环境）
                return 1;
            } else {
                LogUtil.i(TAG, "unknown page:\r\n" + pageHtml);
                // unknown page
                preLoginResponse =null;//pageHtml;
                preCookie = http.getCookie();
                responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_PORTAL, null, null);
                return 4;
            }
        }
    }

    /**
     * check if client is already online (do not need to login again)
     *
     * @param host
     * @param strHtml
     * @return
     */
    private  boolean checkIsLoginned(String host, String strHtml) {
        if (strHtml != null && strHtml.indexOf(Const.NEWS_BAIDU) >= 0) {
            LogUtil.i(TAG, "Already loginned!~");
            return true;
        }
        return false;
    }

    private  boolean processRedirectData(String guideHtml, String cookie) {
        //RunLogCat.i("AuthenPortal", "guide response:\r\n" + guideHtml);
        LogUtil.i(TAG, "guide response:\r\n" + guideHtml);

        String keywordLoginReq = null;
        keywordLoginReq = KEYWORD_CMCCCS;

        keywordLoginReq = keywordLoginReq + SEPARATOR + KEYWORD_LOGINREQ;
        if (guideHtml == null) {
            // 引导页为null，服务器数据异常
            responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_ALL, null, null);
            return false;
        }
        if (guideHtml != null && guideHtml.indexOf(keywordLoginReq) != -1
                || guideHtml != null && guideHtml.indexOf("AppUA") != -1) {
            if (guideHtml.indexOf("AppUA") != -1) {
                String sub = guideHtml.substring(guideHtml.indexOf("<HTML>"),
                        guideHtml.indexOf("</HTML>"));

                if (extractParams_CMCC(
                        sub.replaceAll("<!--", "").replaceAll("-->", ""),
                        CMCC_LOGINFORM_NAME, connParams_perLogin)) {
                    wlanServiceUrl = connParams_perLogin.get("RecordsInfoURL");
                    // 保存预登录页面和Cookie值
                    preLoginResponse = guideHtml;
                    preCookie = cookie;
                    LogUtil.i(TAG, "subguideHtml: "
                            + sub.replaceAll("<!--", "").replaceAll("-->", ""));
                    LogUtil.i(TAG, "connParams_perLogin: "
                            + connParams_perLogin);
                    return true;
                } else {
                    // 引导页解析失败，未知portal异常
                    responseObj = new PortalResponseObj(
                            ErrorMessagesModule.UNKNOWN_PORTAL, null, null);
                    return false;
                }
            } else {
                wlanServiceUrl = getWlanServiceUrl(guideHtml);
                // 保存预登录页面和Cookie值
                preLoginResponse = guideHtml;
                preCookie = cookie;
                if (extractParams_CMCC(guideHtml, CMCC_LOGINFORM_NAME,
                        connParams_perLogin)) {
                    LogUtil.i(TAG, "connParams_perLogin: "
                           + connParams_perLogin);
                    return true;
                } else {
                    // 引导页解析失败，未知portal异常
                    responseObj = new PortalResponseObj(
                            ErrorMessagesModule.UNKNOWN_PORTAL, null, null);
                    return false;
                }
            }
        } else {
            try {
                String redirectLocation = extractRedirectLocation(guideHtml);
                if (redirectLocation != null && redirectLocation.trim().length() > 0) {
                    if (checkCancel()) {
                        LogUtil.i(TAG, "cancelled before send redirect request in processRedirectData.");
                        return false;
                    }
                    LogUtil.i(TAG, "redirect url is:" + redirectLocation);
                    PortalHttp http = new PortalHttp();
                    if (http.sendDataGet(false, redirectLocation)) {
                        return processRedirectData(http.getResponse(), http.getCookie());
                    }
                } else {
                    // 获取PortalUrl失败，未知portal异常
                    responseObj = new PortalResponseObj(ErrorMessagesModule.UNKNOWN_PORTAL, null, null);
                }
            } catch (CMCCWifiAuthException e) {
                // 重定向时发生网络异常
                responseObj = new PortalResponseObj(e);
            }
        }
        return false;
    }

    private  Map<String, String> extractFormParams(String guideHtml) {
        Map<String, String> params = new HashMap<String, String>();
        Parser mHtmlParser = Parser.createParser(guideHtml.toLowerCase(), "gb2312");
        NodeList inputList=null;
        NodeClassFilter inputFilter = new NodeClassFilter(InputTag.class);
        try {
            inputList = mHtmlParser.extractAllNodesThatMatch(inputFilter);
            for (int i = 0; i < inputList.size(); ++i) {
                Node tag = inputList.elementAt(i);

                InputTag inputTag = (InputTag) tag;

                // String attrType = inputTag.getAttribute("TYPE");
                String attrName = inputTag.getAttribute("name");
                String attrValue = inputTag.getAttribute("value");

                if (attrName != null && attrValue != null) {
                    params.put(attrName.trim(), attrValue.trim());
                }
            }

        } catch (ParserException pe) {
            LogUtil.e(TAG, pe.getMessage());
        }
        guideHtml=null;
        mHtmlParser=null;
        inputList=null;
        return params;
    }

    /**
     *
     * @param html
     * @return
     */
    private  String extractPortalUrl(String html) {
        /**
         * <input type="hidden" name="wlanacname" value="0019.0010.100.00">
         * <input type="hidden" name="wlanuserip" value="218.205.219.117">
         * <input type="hidden" name="portalurl"
         * value="http://221.176.1.140/wlan/index.php">
         */
        Map<String, String> params = extractFormParams(html);
        if (params.size() > 0) {
            String portalUrl = params.get(INDICATOR_REDIRECT_PORTALURL);
            String acname = params.get(INDICATOR_LOGIN_AC_NAME);
            String userip = params.get(INDICATOR_LOGIN_USER_IP);
            if (portalUrl != null && portalUrl.length() > 0 && acname != null
                    && acname.length() > 0 && userip != null && userip.length() > 0) {
                StringBuffer location = new StringBuffer(portalUrl);
                location.append("?");
                location.append(INDICATOR_LOGIN_AC_NAME).append("=").append(acname);
                location.append("&");
                location.append(INDICATOR_LOGIN_USER_IP).append("=").append(userip);
                return location.toString();
            }
        }
        return null;
    }

    private  String extractBJAirport(String html) {
        String location = processBJAirport(html);
        if (location != null) {
            /**
             * http://221.176.1.140/wlan/index.php?wlanacname=1032.0010.100.00&
             * wlanuserip=117.130.142.115&wlanacip=117.130.192.65
             */
            int i = location.indexOf("?");
            location = "http://221.176.1.140/wlan/index.php" + location.substring(i);
            LogUtil.i(TAG, "Beijing Airport url is:" + location);
        }
        return location;
    }

    private  String processStringWithQuot(String s) {
        if (s != null && s.length() > 0) {
            if (s.startsWith("\"")) {
                if (s.length() > 1) {
                    s = s.substring(1);
                }
            }
            if (s.endsWith("\"")) {
                if (s.length() > 1) {
                    s = s.substring(0, s.length() - 1);
                }
            }
            if (s.length() > 0) {
                return s;
            }
        }
        return null;
    }

    private  String processBJAirport(String html) {
        /**
         * pw="1234567890";pweb=
         * "http://117.133.0.4/wlan/Wireless_login_index.jsp";
         * acid="1032.0010.100.00"; vid=0 ;mip=117130142063;Gno=0 ;vlanid="0";
         * AC
         * ="                          ";ipm="7582c041";ss1="000d4822177d";ss2=
         * "ffff";ss3="75828e3f";ss4="001882492a88";ss5="117.130.142.63";
         * ss6="117.130.192.65";
         * window.location=pweb+"?wlanacname="+acid+"&wlanacip="
         * +ss6+"&wlanuserip="+ss5;
         */
        if (html != null) {
            String pweb = null;
            String acid = null;
            String ss6 = null;
            String ss5 = null;
            int start = html.indexOf("<!--");
            int end = html.indexOf("-->");
            if (start != -1 && start + 4 < html.length() && end != -1) {
                String temp = html.substring(start + 4, end);
                if (temp != null && temp.length() > 0) {
                    String[] strs = temp.split(";");
                    if (strs != null && strs.length > 0) {
                        for (String v : strs) {
                            if (v != null && v.trim().length() > 0) {
                                v = v.trim();
                                if (v.startsWith("pweb=")) {
                                    if (v.length() > 5) {
                                        pweb = processStringWithQuot(v.substring(5));
                                        LogUtil.i(TAG, "pweb=" + pweb);
                                    }
                                } else if (v.startsWith("acid=")) {
                                    if (v.length() > 5) {
                                        acid = processStringWithQuot(v.substring(5));
                                        LogUtil.i(TAG, "acid=" + acid);
                                    }
                                } else if (v.startsWith("ss6=")) {
                                    if (v.length() > 4) {
                                        ss6 = processStringWithQuot(v.substring(4));
                                        LogUtil.i(TAG, "ss6=" + ss6);
                                    }
                                } else if (v.startsWith("ss5=")) {
                                    if (v.length() > 4) {
                                        ss5 = processStringWithQuot(v.substring(4));
                                        LogUtil.i(TAG, "ss5=" + ss5);
                                    }
                                }
                            }
                        }
                    }

                }
            }
            if (pweb != null && acid != null && ss6 != null && ss5 != null) {
                return pweb + "?wlanacname=" + acid + "&wlanacip=" + ss6 + "&wlanuserip=" + ss5;
            }
        }
        return null;
    }

    private  String extractHref(String html) {
        /**
         * <script language="javascript"> window.location.href=
         * "http://221.176.1.140/wlan/index.php?wlanacname=1037.0010.100.00&wlanuserip=117.134.26.92&ssid=&vlan=4095"
         * ; var PORTALLOGINURL="https://221.176.1.140/wlan/bin/login.pl"; var
         * PORTALLOGOUTURL="https://221.176.1.140/wlan/bin/logout.pl"; </script>
         */
        String PREFIX = "window.location.href";
        int index = html.indexOf(PREFIX);
        if (index != -1) {
            String temp = html.substring(index + PREFIX.length());
            index = temp.indexOf("=");
            if (index != -1 && index < temp.length() - 1) {
                temp = temp.substring(index + 1).trim();
                if (temp.length() > 0) {
                    int start = 0;
                    int end = 1;
                    if (temp.startsWith("\"")) {
                        start = 1;
                        end = temp.indexOf("\"", start);
                    } else {
                        end = temp.indexOf(";", start);
                    }
                    if (end > start) {
                        return temp.substring(start, end);
                    }
                }
            }
        }
        return null;
    }

    private  String extractNextUrl(String html) {
        /**
         * redirect html sample: <HTML> <HEAD> <META HTTP-EQUIV="REFRESH"
         * CONTENT="1;URL=http://117.134.32.234   "> <TITLE> </TITLE> </HEAD>
         * <?xml version="1.0" encoding="UTF-8"?> <WISPAccessGatewayParam
         * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         * xsi:noNamespaceSchemaLocation
         * ="http://www.acmewisp.com/WISPAccessGatewayParam.xsd"> <Proxy>
         * <MessageType>110</MessageType>
         * <NextURL>http://221.176.1.140/wlan/index
         * .php?wlanacip=117.134.32.234&wlanuserip
         * =117.133.202.211&wlanacname=0099.0010.100.00</NextURL>
         * <ResponseCode>200</ResponseCode> </Proxy> </WISPAccessGatewayParam>
         * </HTML>
         */
        int startTagNextURL = html.toLowerCase().indexOf("<nexturl>");
        int endTagNextURL = html.toLowerCase().indexOf("</nexturl>");

        // extract "NextURL" from temp HTML and re-direct
        if (startTagNextURL != -1 && endTagNextURL != -1) {
            return html.substring(startTagNextURL + "<nexturl>".length(), endTagNextURL);
        }
        return null;
    }

    private  String parseAs1stPage(String html) {
        /**
         * the first page <HTML> <HEAD> <TITLE>Cisco Systems Inc. Web
         * Authentication Redirect</TITLE> <META http-equiv="Cache-control"
         * content="no-cache"> <META http-equiv="Pragma" content="no-cache">
         * <META http-equiv="Expires" content="-1"> <META http-equiv="refresh"
         * content="1; URL=https://1.1.1.1/login.html?redirect=www.baidu.com/">
         * </HEAD> </HTML>
         *
         * get https://1.1.1.1/login.html?redirect=www.baidu.com/
         */
        Parser mHtmlParser = Parser.createParser(html, "gb2312");
        NodeList nls =null;
        try {
            NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            nls = mHtmlParser.extractAllNodesThatMatch(metaFilter);
            for (int j = 0; j < nls.size(); ++j) {
                MetaTag meta = (MetaTag) nls.elementAt(j);
                if (VALUE_REFRESH.equalsIgnoreCase(meta.getAttribute(ATTR_HTTP_EQUIV))) {
                    String content = meta.getAttribute(ATTR_CONTENT);
                    int i = content.indexOf(URL_PREFIX1);
                    if (i != -1) {
                        mHtmlParser=null;
                        nls=null;
                        html=null;
                        return content.substring(i + URL_PREFIX1.length());
                    }
                }
            }

        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mHtmlParser=null;
            nls=null;
            html=null;
            return null;
        }
        mHtmlParser=null;
        nls=null;
        html=null;
        return null;
    }


    private  String extractVarUrl(String response) throws CMCCWifiAuthException {
        LogUtil.i(TAG, "AnhuiAuthenProcessor.parseRedirectData:" + response);
        /**
         * the first page <HTML> <HEAD> <TITLE>Cisco Systems Inc. Web
         * Authentication Redirect</TITLE> <META http-equiv="Cache-control"
         * content="no-cache"> <META http-equiv="Pragma" content="no-cache">
         * <META http-equiv="Expires" content="-1"> <META http-equiv="refresh"
         * content="1; URL=https://1.1.1.1/login.html?redirect=www.baidu.com/">
         * </HEAD> </HTML>
         *
         * get: URL=https://1.1.1.1/login.html?redirect=www.baidu.com/
         */
        String redirectUrl = parseAs1stPage(response);
        if (redirectUrl != null) {
            boolean isHttps = false;
            if (redirectUrl.startsWith("https")) {
                isHttps = true;
            }
            PortalHttp http = new PortalHttp();
            if (http.sendDataGet(isHttps, redirectUrl)) {
                String newResponse = http.getResponse();
                /**
                 * get: var url =
                 * "http://211.138.191.50:8080/wlan/WebRedirect?op=cmcc&ac_id=ahhf6&?switch_url=https://1.1.1.1/login.html&ap_mac=00:27:0d:af:da:90&wlan=CMCC"
                 * ;
                 */
                String redirectUrl2 = null;
                if (newResponse != null) {
                    int i = newResponse.indexOf(URL_PREFIX2);
                    if (i != -1) {
                        int start = newResponse.indexOf("\"", i);
                        if (start != -1 && start < newResponse.length() - 1) {
                            int end = newResponse.indexOf("\"", start + 1);
                            if (end != -1) {
                                redirectUrl2 = newResponse.substring(start + 1, end);
                            }
                        }
                    }
                }
                return redirectUrl2;
            }
        }
        return null;
    }


    private  String extractRedirectLocation(String html) throws CMCCWifiAuthException {
        LogUtil.i(TAG, "start extractRedirectLocation...");
        // String formatHtml = removeComment(html);
        String formatHtml = html;
        String location = extractPortalUrl(formatHtml);
        if (location != null) {
            LogUtil.i(TAG, "portalurl is match!");
            return location;
        } else {
            LogUtil.i(TAG, "no portalurl!");
            location = extractBJAirport(formatHtml);
            if (location != null) {
                LogUtil.i(TAG, "Beijing airport is match!");
                return location;
            } else {
                LogUtil.i(TAG, "not Beijing airport!");
                location = extractHref(formatHtml);
                if (location != null) {
                    LogUtil.i(TAG, "window.location.href is match!");
                    return location;
                } else {
                    LogUtil.i(TAG, "no window.location.href!");
                    location = extractNextUrl(formatHtml);
                    if (location != null) {
                        LogUtil.i(TAG, "NextUrl is match!");
                        return location;
                    } else {
                        LogUtil.i(TAG, "no NextUrl!");
                        location = extractVarUrl(formatHtml);
                        if (location != null) {
                            LogUtil.i(TAG, "extractVarUrl is match!");
                            return location;
                        }
                        LogUtil.i(TAG, "no extractVarUrl!");
                    }

                }
            }
        }
        return null;
    }

    private boolean checkCancel() {
        synchronized (isCancelLogin) {
            return isCancelLogin;
        }
    }

    public String getWlanServiceUrl(String html) {
        html = html.replace("|", ";,");
        String arr[] = html.split(";,");
        // 判断字符数组的大小是否有7个，因为套餐订购的url是数组中的第7个
        if (arr != null && arr.length > 6) {
            String strOrderUrl = arr[6].toString();
            return strOrderUrl;
        }
        return null;
    }

    /**
     * remove comments from HTML String
     *
     * @param response
     * @return
     */
    private  String removeComment(String response) {
        String start = "<!--";
        String end = "-->";
        String[] cut1 = response.split(start);
        StringBuffer sb = new StringBuffer();
        for (String temp : cut1) {
            int index = temp.indexOf(end);
            if (index >= 0 && index + end.length() < temp.length()) {
                sb.append(temp.substring(index + end.length()));
            } else {
                sb.append(temp);
            }
        }
        return sb.toString();
    }

    /**
     *
     * @author Administrator
     *
     */
    private class FormFilter extends NodeClassFilter {
        private static final long serialVersionUID = 7347235619802963630L;
        private String formName = null;

        public FormFilter(String formName) {
            super(FormTag.class);
            this.formName = formName;
        }

        @Override
        public boolean accept(Node node) {
            if (super.accept(node)) {
                if (node instanceof FormTag) {
                    FormTag form = (FormTag) node;
                    if (formName != null & formName.equals(form.getFormName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    /**
     *
     * @param guideHtml
     * @param formName
     * @return true: extract specific form "formname", and related form TAG
     *         attributes/value is stored at connParams map false: failure
     */
    private  boolean extractParams_CMCC(String guideHtml, String formName,Map<String, String> connParams) {

        String loginformAction = null;

        // remove html comments
        String tmpGuideHtml = removeComment(guideHtml);

        Parser mHtmlParser = Parser.createParser(tmpGuideHtml, "gb2312");
        FormFilter filter = new FormFilter(formName);
        NodeList formList=null;
        try {
            formList = mHtmlParser.parse(filter);
            LogUtil.e(TAG, "formList:"+formList);
            if (formList != null && formList.size() > 0) {
                Node tag = formList.elementAt(0);
                FormTag formTag = (FormTag) tag;

                connParams.clear();
                if(guideHtml.indexOf("AppUA")!=-1){
                    extractParamsNewInterface(guideHtml);
                }
                // get form action, which is a URL
                loginformAction = formTag.getFormLocation();
                if (loginformAction != null & loginformAction.trim().length() > 0) {
                    connParams.put("action", loginformAction.trim());
                }

                NodeList inputTags = formTag.getFormInputs();
                for (int j = 0; j < inputTags.size(); j++) {
                    Node node = inputTags.elementAt(j);
                    InputTag input = (InputTag) node;
                    String attrName = input.getAttribute("name");
                    String attrValue = input.getAttribute("value");

                    if (attrName != null && attrValue != null) {
                        if (!attrName.trim().equals(INDICATOR_LOGIN_CLIENTTYPE)) {
                            connParams.put(attrName.trim(), attrValue.trim());
                        }
                        // connParams.put(attrName.trim(), attrValue.trim());
                    }
                }
            } else {
                guideHtml=null;
                formList=null;
                mHtmlParser=null;
                return false;
            }
        } catch (ParserException pe) {
            guideHtml=null;
            formList=null;
            mHtmlParser=null;
            return false;
        }
        guideHtml=null;
        formList=null;
        mHtmlParser=null;
        return true;
    }

    //解析接口机接口地址
    public void extractParamsNewInterface(String guideHtml){
        try {
            XmlPullParser parser = Xml.newPullParser();
            InputStream in_nocode = new ByteArrayInputStream(
                    guideHtml
                            .substring(
                                    guideHtml
                                            .indexOf("<Redirect>"),
                                    guideHtml
                                            .indexOf("</Redirect>"))
                            .getBytes());
            LogUtil.i(TAG, "lxd:" + guideHtml);
            parser.setInput(in_nocode, "UTF-8");
            int eventType = parser.getEventType();
            try {
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    LogUtil.e(TAG, "lxd:"+eventType);
                    String nodeName = parser.getName();
                    switch (eventType) {
                        // 文档开始
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        // 开始节点
                        case XmlPullParser.START_TAG:
                            LogUtil.e(TAG, "lxd:"+nodeName);
                            // 判断如果其实节点为LoginURL
                            if ("LoginURL".equals(nodeName)) {
                                // 登陆URL
                                pu.setLgoinURL(parser
                                        .nextText());
//								Log.i("login",
//										"login:"
//												+ pu.getLoginURL());
//								strLoginUrl = pu.getLoginURL();
                                URL = pu.getLoginURL();
                                connParams_perLogin.put("action", pu.getLoginURL());
                            } else if ("LogoffURL"
                                    .equals(nodeName)) {
                                // 下线URL
//								LogUtil.e(TAG, "lxd:"+parser
//										.nextText());
                                pu.setLogoffURL(parser
                                        .nextText());
                                URL = URL + ";"
                                        + pu.getLogoffURL();
                                connParams_perLogin.put("LogoffURL", pu.getLogoffURL());
                            } else if ("DynamicPasswordApplicaitonURL"
                                    .equals(nodeName)) {
                                // 动态密码URL
                                pu.setDynamicPasswordApplicaitonURL(parser
                                        .nextText());
                                URL = URL
                                        + ";"
                                        + pu.getDynamicPasswordApplicaitonURL();
                                connParams_perLogin.put("DynamicPasswordApplicaitonURL", pu.getDynamicPasswordApplicaitonURL());
                            } else if ("AcquireVerifyCodeURL"
                                    .equals(nodeName)) {

                                LogUtil.i(TAG, "需要验证码");
                               /* // 验证码URL
                                pu.setAcquireVerifyCodeURL(parser
                                        .nextText());

                                //验证码
                                VerifyCodeHelper vch = new VerifyCodeHelper();
                                vch.setHost(pu.getAcquireVerifyCodeURL());
                                URL = URL
                                        + ";"
                                        + pu.getAcquireVerifyCodeURL();
                                connParams_perLogin.put("AcquireVerifyCodeURL", pu.getAcquireVerifyCodeURL());*/
                            } else if ("PackageListURL"
                                    .equals(nodeName)) {
                                // 套餐列表URL
                                pu.setPackageListURL(parser
                                        .nextText());
                                URL = URL
                                        + ";"
                                        + pu.getPackageListURL();
                                connParams_perLogin.put("PackageListURL", pu.getPackageListURL());
                            } else if ("RetrievePasswordURL"
                                    .equals(nodeName)) {
                                // 找回密码URL
                                pu.setRetrievePpasswordURL(parser
                                        .nextText());
                                URL = URL
                                        + ";"
                                        + pu.getRetrievePpasswordURL();
                                connParams_perLogin.put("RetrievePasswordURL", pu.getRetrievePpasswordURL());
                            } else if ("RecordsInfoURL"
                                    .equals(nodeName)) {
                                // 上网记录URL
                                pu.setRecordsInfoURL(parser
                                        .nextText());
                                URL = URL
                                        + ";"
                                        + pu.getRecordsInfoURL();
                                connParams_perLogin.put("RecordsInfoURL", pu.getRecordsInfoURL());
                            } else if ("PackageInfoURL"
                                    .equals(nodeName)) {
                                // 我的套餐URL
                                pu.setPackageInfoURL(parser
                                        .nextText());
                                URL = URL
                                        + ";"
                                        + pu.getPackageInfoURL();
                                connParams_perLogin.put("PackageInfoURL", pu.getPackageInfoURL());
                            }
                            break;
                        // 结束节点
                        case XmlPullParser.END_TAG:
                            LogUtil.e(TAG, "lxd"+connParams_perLogin);
                            pu.setALL_URL(URL);
//							if (URL != null) {
//								String[] s = URL.split(";");
//								for (int i = 0; i < s.length; i++) {
//									LogUtil.e(TAG,  "URL:"+URL);
//								}
//							}
                            break;
                        default:
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (NumberFormatException e) {
                LogUtil.e(TAG, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

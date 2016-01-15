package com.zvin.wificonnect.migration;

import android.content.Context;

import com.zvin.wificonnect.R;

import java.util.HashMap;
import java.util.Map;
/**
 * 错误信息对象
 *
 */
public class ErrorMessagesModule {
//	public static int NETWORK_ERROR = 888;
//	public static int NETWORK_TIMEOUT = 889;
	public static int UNKNOWN_PORTAL = 1997;
	public static int UNKNOWN_ALL = 1992;
	private static Map<Integer, Integer> mCommonHttpExceptionMsg = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mLoginMessage = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mLogoutMessage = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mPwdMessage = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mRetrievePwdMessage = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mGetPackageMessage = new HashMap<Integer, Integer> ();
	private static Map<Integer, Integer> mHistoryRecordMessage = new HashMap<Integer, Integer> ();
	//获取账号（即免费体验时获取验证码）失败
	private static Map<Integer, Integer> mFreeVerifyCodeMessage = new HashMap<Integer, Integer> ();
	//免费体验登录失败
	private static Map<Integer, Integer> mFreeLoginMessage = new HashMap<Integer, Integer> ();
	//免费体验时下线失败
	private static Map<Integer, Integer> mFreeLogoutMessage = new HashMap<Integer, Integer> ();
	
	//积分墙失败
	private static Map<Integer, Integer> offerWallErrorMessage = new HashMap<Integer, Integer> ();
	
	static {
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_TIMEOUT, R.string.error_common_timeout);
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_IO, R.string.error_common_io);
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_UNKNOWN_HOST, R.string.error_common_hostunknown);
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_NOTVALID_RESCODE, R.string.error_common_rescode_invalid);
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_RESCODE_NOTOK, R.string.error_common_rescode_notok);
		mCommonHttpExceptionMsg.put(CMCCWifiAuthException.EXCEPTION_TYPE_OTHER, R.string.error_common_other_exception);
	}
	
	static {
		
		
		offerWallErrorMessage.put(108, R.string.score_umc_error_108);
		offerWallErrorMessage.put(109, R.string.score_umc_error_109);
		offerWallErrorMessage.put(103, R.string.score_umc_error_103);
		offerWallErrorMessage.put(104, R.string.score_umc_error_104);
		offerWallErrorMessage.put(801, R.string.score_umc_error_801);
	}
	
	static {
		mLoginMessage.put(1, R.string.error_login1);
		mLoginMessage.put(2, R.string.error_login2);
		mLoginMessage.put(3, R.string.error_login3);
		mLoginMessage.put(7, R.string.error_login7);
		mLoginMessage.put(8, R.string.error_login8);
		mLoginMessage.put(26, R.string.error_login26);
		mLoginMessage.put(105, R.string.error_login105);
		mLoginMessage.put(106, R.string.error_login106);
		mLoginMessage.put(107, R.string.error_login107);
		mLoginMessage.put(108, R.string.error_login108);
		mLoginMessage.put(109, R.string.error_login109);
		mLoginMessage.put(110, R.string.error_login110);
		mLoginMessage.put(111, R.string.error_login111);
		mLoginMessage.put(UNKNOWN_PORTAL, R.string.error_login_unknown_portal);	//1997
		mLoginMessage.put(UNKNOWN_ALL, R.string.error_login_unknown_all);	// 1992，服务器数据异常
//		mLoginMessage.put(NETWORK_ERROR, R.string.error_login_other);
//		mLoginMessage.put(NETWORK_TIMEOUT, R.string.error_login_timeout);
		
		mLogoutMessage.put(-1, R.string.error_logout_other);
		mLogoutMessage.put(7, R.string.error_logout7);
		mLogoutMessage.put(9, R.string.error_logout9);
		mLogoutMessage.put(11, R.string.error_logout11);
		mLogoutMessage.put(18, R.string.error_logout18);
		
		mPwdMessage.put(-1, R.string.error_password999);
		mPwdMessage.put(1, R.string.error_password1);
		mPwdMessage.put(2, R.string.error_password2);
		mPwdMessage.put(3, R.string.error_password3);
		mPwdMessage.put(4, R.string.error_password4);
		mPwdMessage.put(5, R.string.error_password5);
		mPwdMessage.put(10, R.string.error_password10);
		mPwdMessage.put(20, R.string.error_password20);
		mPwdMessage.put(22, R.string.error_password22);
		mPwdMessage.put(23, R.string.error_password23);
		mPwdMessage.put(24, R.string.error_password24);
		mPwdMessage.put(100, R.string.error_password100);
		mPwdMessage.put(101, R.string.error_password101);
		mPwdMessage.put(110, R.string.error_password110);
		mPwdMessage.put(111, R.string.error_password111);
		
		mRetrievePwdMessage.put(-1, R.string.request_password_fail);
		mRetrievePwdMessage.put(1, R.string.request_password_fail1);
		mRetrievePwdMessage.put(2, R.string.request_password_fail2);
		mRetrievePwdMessage.put(5, R.string.request_password_fail5);
		mRetrievePwdMessage.put(10, R.string.request_password_fail10);
		mRetrievePwdMessage.put(20, R.string.request_password_fail20);
		mRetrievePwdMessage.put(100, R.string.request_password_fail100);
		mRetrievePwdMessage.put(101, R.string.request_password_fail101);
		mRetrievePwdMessage.put(102, R.string.request_password_fail8012);
		
		mGetPackageMessage.put(1, R.string.package_error_1);
		mGetPackageMessage.put(3, R.string.package_error_3);
		mGetPackageMessage.put(4, R.string.package_error_4);
		mGetPackageMessage.put(9, R.string.package_error_9);
		mGetPackageMessage.put(20, R.string.package_error_20);
		mGetPackageMessage.put(100, R.string.package_error_100);
		mGetPackageMessage.put(101, R.string.package_error_101);
		mGetPackageMessage.put(122, R.string.package_error_122);
		
		mHistoryRecordMessage.put(1, R.string.history_record_error_1);
		mHistoryRecordMessage.put(2, R.string.history_record_error_2);
		mHistoryRecordMessage.put(3, R.string.history_record_error_3);
		mHistoryRecordMessage.put(4, R.string.history_record_error_4);
		mHistoryRecordMessage.put(5, R.string.history_record_error_5);
		mHistoryRecordMessage.put(9, R.string.history_record_error_9);
		mHistoryRecordMessage.put(20, R.string.history_record_error_20);
		mHistoryRecordMessage.put(100, R.string.history_record_error_100);
		mHistoryRecordMessage.put(101, R.string.history_record_error_101);
		mHistoryRecordMessage.put(102, R.string.history_record_error_102);
		mHistoryRecordMessage.put(103, R.string.history_record_error_103);
		mHistoryRecordMessage.put(122, R.string.history_record_error_122);
		
//		mFreeVerifyCodeMessage.put(1, R.string.error_free_verify_code1);
//		mFreeVerifyCodeMessage.put(2, R.string.error_free_verify_code2);
//		mFreeVerifyCodeMessage.put(3, R.string.error_free_verify_code3);
//		mFreeVerifyCodeMessage.put(4, R.string.error_free_verify_code4);
//		mFreeVerifyCodeMessage.put(10, R.string.error_free_verify_code10);
//		mFreeVerifyCodeMessage.put(20, R.string.error_free_verify_code20);
//		mFreeVerifyCodeMessage.put(100, R.string.error_free_verify_code100);
//		mFreeVerifyCodeMessage.put(101, R.string.error_free_verify_code101);
//		mFreeVerifyCodeMessage.put(115, R.string.error_free_verify_code115);
//		
//		mFreeLoginMessage.put(1, R.string.error_free_login1);
//		mFreeLoginMessage.put(2, R.string.error_free_login2);
//		mFreeLoginMessage.put(3, R.string.error_free_login3);
//		mFreeLoginMessage.put(7, R.string.error_free_login7);
//		mFreeLoginMessage.put(8, R.string.error_free_login8);
//		mFreeLoginMessage.put(15, R.string.error_free_login15);
//		mFreeLoginMessage.put(17, R.string.error_free_login17);
//		mFreeLoginMessage.put(26, R.string.error_free_login26);
//		mFreeLoginMessage.put(40, R.string.error_free_login40);
//		mFreeLoginMessage.put(55, R.string.error_free_login55);
//		mFreeLoginMessage.put(105, R.string.error_free_login105);
//		mFreeLoginMessage.put(106, R.string.error_free_login106);
//		mFreeLoginMessage.put(107, R.string.error_free_login107);
//		mFreeLoginMessage.put(108, R.string.error_free_login108);
//		mFreeLoginMessage.put(109, R.string.error_free_login109);
//		mFreeLoginMessage.put(112, R.string.error_free_login112);
//		mFreeLoginMessage.put(113, R.string.error_free_login113);
//		mFreeLoginMessage.put(114, R.string.error_free_login114);
//		mFreeLoginMessage.put(115, R.string.error_free_login115);
//		mFreeLoginMessage.put(116, R.string.error_free_login116);
//		mFreeLoginMessage.put(118, R.string.error_free_login118);
//		mFreeLoginMessage.put(119, R.string.error_free_login119);
//		mFreeLoginMessage.put(120, R.string.error_free_login120);
//		
//		mFreeLogoutMessage.put(7, R.string.error_free_logout7);
//		mFreeLogoutMessage.put(9, R.string.error_free_logout9);
//		mFreeLogoutMessage.put(11, R.string.error_free_logout11);
//		mFreeLogoutMessage.put(18, R.string.error_free_logout18);
		
	}
	
	public static String getLoginErrorMessage(Context context, PortalResponseObj res) {
		if (res != null) {
			Integer resId = mLoginMessage.get(res.getCode());
			if (resId == null) {
				// 网络异常Code
				if (res.getExceptionDetail() != null) {
					resId = mCommonHttpExceptionMsg.get(res.getExceptionDetail().getType());
				}
			}
			// 返回码未找到
			// 如果有返回码，无文字，则添加提示语“网络异常，请咨询当地10086。（错误代码：【按平台返回码】）”
			// 如果有返回码有文字，则直接按照 提示语。（错误代码：【按平台返回码】）的形式显示
			// 如果有文字无返回码，则只显示 提示语。（错误代码：1998）
			if (resId == null) {
				String msg = "";
				if (res.getMsg() != null && res.getMsg().length() > 0) {
					// 有原因说明
					msg += res.getMsg();
				} else {
					// 没有原因说明，使用默认提示
					msg += context.getString(R.string.error_login_default_desp);
				}
				/*String codeStr = context.getString(R.string.error_login_default_code);
				if (res.getCode() > 0) {
					// 有登录结果code，（错误代码：xxx）
					codeStr = codeStr.replace("$code", "" + res.getCode());
				} else {
					// 没有登录结果code，有原因说明，（错误代码：1998）
					if (res.getMsg() != null && res.getMsg().length() > 0) {
						codeStr = codeStr.replace("$code", "1998");
					} else {
						// 没有登录结果code，也没有原因说明，（错误代码：1992）
						codeStr = codeStr.replace("$code", "1992");
					}
				}
				msg += codeStr;*/
				return msg;
			}
			return context.getString(resId);
		} else {
			// 彻底未知
			return context.getString(R.string.error_login_unknown_all);
		}
	}
	
	public static String getSocreError(Context context,int code)
	{
		if(offerWallErrorMessage.containsKey(code))
		{
		int resID=offerWallErrorMessage.get(code);
		return context.getString(resID);
		}
		else
		{
			return context.getString(R.string.score_umc_error_other);
		}
	}
	
	public static String getFreeLoginErrorMessage(Context context, PortalResponseObj res) {
		if (res != null) {
			Integer resId = null;
			// 网络异常Code, CXXX
			if (res.getExceptionDetail() != null) {
				resId = mCommonHttpExceptionMsg.get(res.getExceptionDetail().getType());
				return context.getString(resId);
			} else {
				// 返回码未找到
				// 如果有返回码，无文字，则添加提示语“网络异常，请咨询当地10086。（错误代码：【按平台返回码】）”
				// 如果有返回码有文字，则直接按照 提示语。（错误代码：【按平台返回码】）的形式显示
				// 如果有文字无返回码，则只显示 提示语，不补充错误代码。
				
				String codeStr = context.getString(R.string.error_login_default_code);
				if (res.getCode() == UNKNOWN_PORTAL) {
					// 1997 Portal页面不符合规范
					codeStr = codeStr.replace("$code", "13997");
				} else if (res.getCode() == UNKNOWN_ALL) {
					// 1992 Portal数据异常或其他未知错误
					codeStr = codeStr.replace("$code", "13992");
				} else if (res.getCode() > 0) {
					// 有登录结果code，（错误代码：13xxx）
					String resCode = String.valueOf(res.getCode());
					//平台返回码不足三位前面补零
					if(resCode.length()==2){
						resCode = "0" + resCode;
					}else if(resCode.length()==1){
						resCode = "00" + resCode;
					}
					codeStr = codeStr.replace("$code", "13" + resCode);
				} else {
					// -1
					// 没有登录结果code，有原因说明，不补充错误代码
					if (res.getMsg() != null && res.getMsg().length() > 0) {
						codeStr = "";
					} else {
						// 没有登录结果code，也没有原因说明，（错误代码：1992）
						codeStr = codeStr.replace("$code", "13992");
					}
				}
				
				String msg = "";
				if (res.getCode() == 8 || res.getCode() == 125 || res.getCode() == 126) {
					msg += context.getString(R.string.error_free_login_default);
				} else if (res.getMsg() != null && res.getMsg().length() > 0) {
					// 有原因说明
					msg += res.getMsg();
				} else {
					// 没有原因说明，使用默认提示
					msg += context.getString(R.string.error_login_default_desp);
				}
				msg += codeStr;
				return msg;
			}
		} else {
			// 彻底未知，13992
			return context.getString(R.string.error_free_login_unknown_all);
		}
	}
	
	public static int getLogoutErrorMessage(PortalResponseObj res) {
		return R.string.error_logout_other;

		//是否为新接口机
		/*if(CMCCApplication.capp.getCMCCManager().getMperferce().is_new_interface){
			return R.string.error_logout_other;
		}*/
		/*if (res != null) {
			Integer resId = mLogoutMessage.get(res.getCode());
			if (resId == null) {
				// 网络异常Code
				if (res.getExceptionDetail() != null) {
					resId = mCommonHttpExceptionMsg.get(res.getExceptionDetail().getType());
				}
			}
			if (resId == null) {
				if (res.getMsg() != null && res.getMsg().length() > 0) {
					return -1;
				}
				return R.string.error_logout_other;
			}
			return resId ;
		} else {
			return R.string.error_logout_other;
		}*/
	}
	
	
	public static String getFreeLogoutErrorMessage(Context context, PortalResponseObj res) {
		if (res != null) {
			Integer resId = null;
			// 网络异常Code, CXXX
			if (res.getExceptionDetail() != null) {
				resId = mCommonHttpExceptionMsg.get(res.getExceptionDetail().getType());
				return context.getString(resId);
			} else {
				// 如果有返回码，无文字，则添加提示语“下线失败，请关闭WLAN开关，15分钟后自动下线。（错误代码：【按平台返回码】）”
				// 如果有返回码有文字，则直接按照 提示语。（错误代码：【按平台返回码】）的形式显示
				// 如果有文字无返回码，则只显示 提示语，不补充错误代码。
				// 未知错误，一律14999
				
				String codeStr = context.getString(R.string.error_login_default_code);
				if (res.getCode() > 0) {
					// 有下线结果code，（错误代码：14xxx）
					String resCode = String.valueOf(res.getCode());
					//平台返回码不足三位前面补零
					if(resCode.length()==2){
						resCode = "0" + resCode;
					}else if(resCode.length()==1){
						resCode = "00" + resCode;
					}
					codeStr = codeStr.replace("$code", "14" + resCode);
				} else {
					// -1
					// 没有下线结果code，有原因说明，不补充错误代码
					if (res.getMsg() != null && res.getMsg().length() > 0) {
						codeStr = "";
					} else {
						// 没有下线结果code，也没有原因说明，（错误代码：14999）
						codeStr = codeStr.replace("$code", "14999");
					}
				}
				
				String msg = "";
				if (res.getMsg() != null && res.getMsg().length() > 0) {
					// 有原因说明
					msg += res.getMsg();
				} else {
					// 没有原因说明，使用默认提示
					msg += context.getString(R.string.error_freelogout_default_desp);
				}
				
				msg += codeStr;
				return msg;
			}
		} else {
			// 彻底未知，14999
			return context.getString(R.string.error_free_logout999);
		}
	}
	
	public static int getPwdErrorMessage(PortalResponseObj res) {
		if (res != null) {
			Integer resId = mPwdMessage.get(res.getCode());
			if (resId == null) {
				// 网络异常Code
				if (res.getExceptionDetail() != null) {
					resId = mCommonHttpExceptionMsg.get(res.getExceptionDetail().getType());
				}
			}
			if (resId == null) {
				if (res.getMsg() != null && res.getMsg().length() > 0) {
					return -1;
				}
				return R.string.error_password999;
			}
			return resId ;
		} else {
			return R.string.error_password999;
		}
	}
	
	public static String getFreeVerifyCodeMessage(Context context, String code, String desp) {
		String codeStr = context.getString(R.string.error_login_default_code);
		if (code != null && code.length() > 0) {
			// 有获取验证码结果code，（错误代码：12xxx）
			int codeInt = 0;
			try {
				codeInt = Integer.valueOf(code);
			} catch (NumberFormatException e) {
			}
			if (codeInt > 0) {
				String resCode = code;
				//平台返回码不足三位前面补零
				if(resCode.length()==2){
					resCode = "0" + resCode;
				}else if(resCode.length()==1){
					resCode = "00" + resCode;
				}
				codeStr = codeStr.replace("$code", "12" + resCode);
			} else {
				// code为负数或非数字类型，直接显示不转译
				codeStr = codeStr.replace("$code", code);
			}
		} else {
			// null
			// 没有获取验证码结果code，有原因说明，不补充错误代码
			if (desp != null && desp.length() > 0) {
				codeStr = "";
			} else {
				// 没有获取验证码结果code，也没有原因说明，（错误代码：12999）
				codeStr = codeStr.replace("$code", "12999");
			}
		}
		
		String msg = "";
		if (desp != null && desp.length() > 0) {
			// 有原因说明
			msg += desp;
		} else {
			// 没有原因说明，使用默认提示
			msg += context.getString(R.string.error_freeverify_default_desp);
		}
		msg += codeStr;
		return msg;
	}
	
	public static int getRetrievePwdErrorMessage(int code) {
		Integer resId = mRetrievePwdMessage.get(code);
		if (resId == null) {
			return R.string.request_password_fail;
		}
		return resId ;
	}
	
	public static int getGetPackageMessage(int code) {
		Integer resId = mGetPackageMessage.get(code);
		if (resId == null) {
			return R.string.get_package_failed;
		}
		return resId ;
	}
	
	public static int getHistoryRecordMessage(int code) {
		Integer resId = mHistoryRecordMessage.get(code);
		if (resId == null) {
			return R.string.history_record_error;
		}
		return resId ;
	}
}

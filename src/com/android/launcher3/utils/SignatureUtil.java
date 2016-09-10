package com.android.launcher3.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author jiang
 * 签名工具类
 */
public class SignatureUtil {
	private static final String TAG = "SignatureUtil";
	
	/**
	 * 获取自身签名信息
	 * @return
	 */
	public static String getSelfSignature(Context context) {
		try {
			String strSignature = getApkSignatureMD5WithPackageName(context.getApplicationContext(), context.getPackageName());
			Log.i(TAG, "getSelfSignature->" + strSignature);

			if (TextUtils.isEmpty(strSignature)) {
				return "";
			} else {
				return strSignature;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getSelfSignature", e);
			return "";
		}
	}
	
	/**
	 * 获取apk签名厂商
	 * @param apkPath
	 * @return
	 */
	public static String getSignatureCompony(Signature signature) {
		try {
			X509Certificate sCertificate = getSingXCertificate(signature.toByteArray());
			if (sCertificate != null) {
				String issuer = sCertificate.getIssuerDN().toString();
				String[] tempArray = issuer.split(",");
				
				for (String string : tempArray) {
					string = string.toLowerCase(Locale.getDefault());
					if (string.contains("o=")) {
						string = string.substring(string.indexOf("o=") + 2);
						return string;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getIssureCompony", e);
		}
		return null;
	}
	
	private static X509Certificate getSingXCertificate(byte[] signature) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(signature));
			return cert;
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getApkIssuer", e);
		}
		return null;
	}
	
	/**
	 * 获取系统签名厂商
	 * @param context
	 * @return
	 */
	public static ArrayList<String> getSystemSignatureCompony(Context context) {
		//先从sp中取出来判断，没有则获取
		
		ArrayList<String> signatureList = new ArrayList<String>();
		
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
			
			if (packageInfo != null) {
				Signature[] signatures = packageInfo.signatures;
				if (signatures != null && signatures.length > 0) {
					String strCompony = getSignatureCompony(signatures[0]);
					if (strCompony != null && !signatureList.contains(strCompony)) {
						signatureList.add(strCompony);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (signatureList.size() > 0) {
//			保存到sp中
		}
		return signatureList;
	}
	
	/**
	 * 获取apk签名
	 */
	public static Signature getApkSignatureWithPackageName(Context context, String packageName) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			
			if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
				 return packageInfo.signatures[0];
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取apk签名
	 */
	public static Signature getApkSignatureWithPackageInfo(Context context, PackageInfo packageInfo) {
		try {
			if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
				 return packageInfo.signatures[0];
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取apk签名
	 */
	public static String getApkSignatureMD5WithPackageName(Context context, String pkg) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);

			if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
				return getSignatureMD5(packageInfo.signatures[0].toByteArray());
			}
		} catch (Exception e) {
			Log.w(TAG, "getApkSignatureWithPackageName", e);
		}
		return null;
	}
	
	private static String getSignatureMD5(byte[] signature) {
		try {
			return MD5Util.getMD5(signature);
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getSignatureMD5", e);
			return "";
		}
	}
}

package com.android.launcher3.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.android.launcher3.LauncherApplication;

/**
 * 打印日志类
 */
public class Log {
	private static boolean DEBUG = false;
	private static final String NAME_LOG_FILE = "log.txt";
    private static final String NAME_BACKUP_LOG_FILE = "logbackup.txt";
    
	private Object lock = new Object();
	
	private final int KB = 1024;
	private final int M  = KB * KB;
    private long maxFileSize = M * 4;
	
    private File logFile;
    private OutputStream os;
    private String logDirPath;
    
    private static Log mInstance;
    
    private Log() {}
    
    public static Log getInstance() {
    	if(mInstance == null) {
    		mInstance = new Log();
    	}
    	return mInstance;
    }
    
	/**
	 * Send an INFO log message
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 */
	public static void i(String tag, String msg) {
		try {
			if(DEBUG){
				android.util.Log.i(tag, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a INFO log message and log the exception
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	public static void i(String tag, String msg, Throwable tr) {
		try {
			if(DEBUG) {
				android.util.Log.i(tag, msg, tr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a DEBUG log message
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 */
	public static void d(String tag, String msg) {
		try {
			if(DEBUG) {
				android.util.Log.d(tag, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a DEBUG log message and log the exception
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	public static void d(String tag, String msg, Throwable tr) {
		try {
			if(DEBUG) {
				android.util.Log.d(tag, msg, tr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Send a {@link #WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
	public static void w(String tag, String msg) {
		try {
			android.util.Log.w(tag, msg);
			if(DEBUG) {
				Log.getInstance().saveLogToFile(tag, msg, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * Send a WARN log message and log the exception
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	public static void w(String tag, String msg, Throwable tr) {
		try {
			android.util.Log.w(tag, msg, tr);
			if(DEBUG) {
				Log.getInstance().saveLogToFile(tag, msg, tr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send an ERROR log message
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	public static void e(String tag, String msg) {
		try {
			android.util.Log.e(tag, msg);
			Log.getInstance().saveLogToFile(tag, msg, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a ERROR log message and log the exception
	 * @param sClass The class object
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	public static void e(String tag, String msg, Throwable tr) {
		try {
			android.util.Log.e(tag, msg, tr);
			Log.getInstance().saveLogToFile(tag, msg, tr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取异常的堆栈信息
	 * @param exception
	 * @return
	 */
	public static String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}
	
    /**
     * Init the logger
     */
    private void initLogFile() {
        synchronized(lock) {
            try {
        		logDirPath = LauncherApplication.getInstance().getLogCacheDir();
                logFile = new File(logDirPath, NAME_LOG_FILE);
                if(!logFile.exists()){
                	logFile.createNewFile();
                }
                os = new FileOutputStream(logFile, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Close connection and streams
     */
	private void closeLogFile() {
        synchronized(lock) {
            try {
                if (os != null) {
                    os.close();
                    os = null;
                }
                if (logFile != null) {
                	logFile = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	/**
	 * 将日志保存到本地
	 * @param sClass
	 * @param message
	 * @param tr
	 */
	private void saveLogToFile(String tag, String message, Throwable tr) {
		synchronized (lock) {
			try {
				initLogFile();
				if (os != null) {
					Date now = new Date();
					StringBuffer logMsg = new StringBuffer(now.toString());
					
					if(tag != null) {
						logMsg.append("[");
						logMsg.append(tag);
						logMsg.append("]");
					}
					
					logMsg.append(message);
					logMsg.append("\r\n");
					
					if(tr != null) {
						logMsg.append(getStackTrace(tr));
						logMsg.append("\r\n");
					}
					
					os.write(logMsg.toString().getBytes());
					os.flush();
					if (logFile.length() > maxFileSize) {
						try {
							String oldFileName = logDirPath + File.separator + NAME_BACKUP_LOG_FILE;
							File oldFile = new File(oldFileName);
							Util.deleteFile(oldFile);
							logFile.renameTo(new File(oldFileName));
							logFile = null;
							// Reopen the file
							initLogFile();
						} catch (Exception ioe) {
							ioe.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logFile = null;
				initLogFile();
			} finally {
				closeLogFile();
			}
		}
	}
	
}

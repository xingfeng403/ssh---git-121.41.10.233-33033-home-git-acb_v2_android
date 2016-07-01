package com.youtu.acb.util;

import android.graphics.Bitmap;
import android.os.Environment;

import com.youtu.acb.AcbApplication;
import com.youtu.acb.common.Settings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * 与文件相关的类,主要负责文件的读写
 * 
 * 
 */
public final class FileUtil
{

	/**
	 * 文件保存路径
	 */
	public static final String file = Environment.getExternalStorageDirectory()
			.getPath();
	/**
	 * 图片缓存文件保存路径
	 */
	public static final String fileImages = Settings.APK_SAVE;  // xf  改于2015.8.18
	/**
	 * 照片缓存文件保存路径
	 */
	public static final String fileCamera = file + "/mami/cameras/";

	// ------------------------------ 手机系统相关 ------------------------------
	public static final String NEWLINE = System.getProperty("line.separator");// 系统的换行符
	public static final String APPROOT = "UMMoka";// 程序的根目录
	public static final String ASSERT_PATH = "file:///android_asset";// apk的assert目录
	public static final String RES_PATH = "file:///android_res";// apk的assert目录

	// ----------------------------------存放文件的路径后缀------------------------------------
	public static final String CACHE_IMAGE_SUFFIX = File.separator + APPROOT
			+ File.separator + "images" + File.separator;
	public static final String CACHE_VOICE_SUFFIX = File.separator + APPROOT
			+ File.separator + "voice" + File.separator;
	public static final String CACHE_MATERIAL_SUFFIX = File.separator + APPROOT
			+ File.separator + "material" + File.separator;
	public static final String LOG_SUFFIX = File.separator + APPROOT
			+ File.separator + "Log" + File.separator;

	// ------------------------------------数据的缓存目录-------------------------------------------------------
	public static String SDCARD_PAHT;// SD卡路径
	public static String LOCAL_PATH;// 本地路径,即/data/data/目录下的程序私有目录
	public static String CURRENT_PATH = "";// 当前的路径,如果有SD卡的时候当前路径为SD卡，如果没有的话则为程序的私有目录

	static
	{
		init();
	}

	public static void init()
	{
		SDCARD_PAHT = Environment.getExternalStorageDirectory().getPath();// SD卡路径
		LOCAL_PATH = AcbApplication.getInstance().getApplicationContext()
				.getFilesDir().getAbsolutePath();// 本地路径,即/data/data/目录下的程序私有目录

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
		{
			CURRENT_PATH = SDCARD_PAHT;
		} else
		{
			CURRENT_PATH = LOCAL_PATH;
		}
	}

	/**
	 * 得到与当前存储路径相反的路径(当前为/data/data目录，则返回/sdcard目录;当前为/sdcard，则返回/data/data目录)
	 * 
	 * @return
	 */
	public static String getDiffPath()
	{
		if (CURRENT_PATH.equals(SDCARD_PAHT))
		{
			return LOCAL_PATH;
		}
		return SDCARD_PAHT;
	}

	public static String getDiffPath(String pathIn)
	{
		return pathIn.replace(CURRENT_PATH, getDiffPath());
	}

	// ------------------------------------文件的相关方法--------------------------------------------
	/**
	 * 将数据写入一个文件
	 * 
	 * @param destFilePath
	 *            要创建的文件的路径
	 * @param data
	 *            待写入的文件数据
	 * @param startPos
	 *            起始偏移量
	 * @param length
	 *            要写入的数据长度
	 * @return 成功写入文件返回true,失败返回false
	 */
	public static boolean writeFile(String destFilePath, byte[] data,
			int startPos, int length)
	{
		try
		{
			if (!createFile(destFilePath))
			{
				return false;
			}
			FileOutputStream fos = new FileOutputStream(destFilePath);
			fos.write(data, startPos, length);
			fos.flush();
			if (null != fos)
			{
				fos.close();
				fos = null;
			}
			return true;

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 从一个输入流里写文件
	 * 
	 * @param destFilePath
	 *            要创建的文件的路径
	 * @param in
	 *            要读取的输入流
	 * @return 写入成功返回true,写入失败返回false
	 */
	public static boolean writeFile(String destFilePath, InputStream in)
	{
		try
		{
			if (!createFile(destFilePath))
			{
				return false;
			}
			FileOutputStream fos = new FileOutputStream(destFilePath);
			int readCount = 0;
			int len = 1024;
			byte[] buffer = new byte[len];
			while ((readCount = in.read(buffer)) != -1)
			{
				fos.write(buffer, 0, readCount);
			}
			fos.flush();
			if (null != fos)
			{
				fos.close();
				fos = null;
			}
			if (null != in)
			{
				in.close();
				in = null;
			}
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean appendFile(String filename, byte[] data, int datapos,
			int datalength)
	{
		try
		{

			createFile(filename);

			RandomAccessFile rf = new RandomAccessFile(filename, "rw");
			rf.seek(rf.length());
			rf.write(data, datapos, datalength);
			if (rf != null)
			{
				rf.close();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 读取文件，返回以byte数组形式的数据
	 * 
	 * @param filePath
	 *            要读取的文件路径名
	 * @return
	 */
	public static byte[] readFile(String filePath)
	{
		try
		{
			if (isFileExist(filePath))
			{
				FileInputStream fi = new FileInputStream(filePath);
				return readInputStream(fi);
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从一个数量流里读取数据,返回以byte数组形式的数据。 </br></br>
	 * 需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，
	 * 但如果是用于网络操作，就经常会遇到一些麻烦(available()方法的问题)。所以如果是网络流不应该使用这个方法。
	 * 
	 * @param in
	 *            要读取的输入流
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream in)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			byte[] b = new byte[in.available()];
			int length = 0;
			while ((length = in.read(b)) != -1)
			{
				os.write(b, 0, length);
			}

			b = os.toByteArray();

			in.close();
			in = null;

			os.close();
			os = null;

			return b;

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 读取网络流
	 * 
	 * @param in
	 * @return
	 */
	public static byte[] readNetWorkInputStream(InputStream in)
	{
		ByteArrayOutputStream os = null;
		try
		{
			os = new ByteArrayOutputStream();

			int readCount = 0;
			int len = 1024;
			byte[] buffer = new byte[len];
			while ((readCount = in.read(buffer)) != -1)
			{
				os.write(buffer, 0, readCount);
			}

			in.close();
			in = null;

			return os.toByteArray();

		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != os)
			{
				try
				{
					os.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				os = null;
			}
		}
		return null;
	}

	/**
	 * 将一个文件拷贝到另外一个地方
	 * 
	 * @param sourceFile
	 *            源文件地址
	 * @param destFile
	 *            目的地址
	 * @param shouldOverlay
	 *            是否覆盖
	 * @return
	 */
	public static boolean copyFiles(String sourceFile, String destFile,
			boolean shouldOverlay)
	{
		try
		{
			if (shouldOverlay)
			{
				deleteFile(destFile);
			}
			FileInputStream fi = new FileInputStream(sourceFile);
			writeFile(destFile, fi);
			return true;
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param filePath
	 *            路径名
	 * @return
	 */
	public static boolean isFileExist(String filePath)
	{
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * 判断是否已经安装SD卡
	 * 
	 * @return
	 */
	public static boolean isSDCardExist()
	{
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);

	}

	/**
	 * 创建一个文件，创建成功返回true
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean createFile(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if (!file.exists())
			{
				if (!file.getParentFile().exists())
				{
					file.getParentFile().mkdirs();
				}

				return file.createNewFile();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 删除一个文件
	 * 
	 * @param filePath
	 *            要删除的文件路径名
	 * @return true if this file was deleted, false otherwise
	 */
	public static boolean deleteFile(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if (file.exists())
			{
				return file.delete();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 删除 directoryPath目录下的所有文件，包括删除删除文件夹
	 * 
	 */
	public static void deleteDirectory(File dir)
	{
		if (dir.isDirectory())
		{
			File[] listFiles = dir.listFiles();
			for (int i = 0; i < listFiles.length; i++)
			{
				deleteDirectory(listFiles[i]);
			}
		}
		dir.delete();
	}

	/**
	 * 字符串转流
	 * 
	 * @param str
	 * @return
	 */
	public static InputStream String2InputStream(String str)
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}

	/**
	 * 流转字符串
	 * 
	 * @param is
	 * @return
	 */
	public static String inputStream2String(InputStream is)
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";

		try
		{
			while ((line = in.readLine()) != null)
			{
				buffer.append(line);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return buffer.toString();
	}

	// 批量更改文件后缀
	public static void reNameSuffix(File dir, String oldSuffix, String newSuffix)
	{
		if (dir.isDirectory())
		{
			File[] listFiles = dir.listFiles();
			for (int i = 0; i < listFiles.length; i++)
			{
				reNameSuffix(listFiles[i], oldSuffix, newSuffix);
			}
		} else
		{
			dir.renameTo(new File(dir.getPath().replace(oldSuffix, newSuffix)));
		}
	}

	public static void writeImage(Bitmap bitmap, String destPath, int quality)
	{
		try
		{
			FileUtil.deleteFile(destPath);
			if (FileUtil.createFile(destPath))
			{
				FileOutputStream out = new FileOutputStream(destPath);
				if (bitmap.compress(Bitmap.CompressFormat.PNG, quality, out))
				{
					out.flush();
					out.close();
					out = null;
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 获取URL路径中的文件名称
	 * 
	 * @param url
	 * @return 文件名称
	 */
	public static String urlToFileName(String url)
	{
		String ftype = url.substring(url.lastIndexOf("."));
		String fName = ftype.substring(ftype.lastIndexOf("/") + 1);
		// String fName1 = fName.substring(0, url.lastIndexOf("/") + 1);
		// return fName.replace(".", "") + ftype;
		return fName.replace(".", "");
	}

	public static String urlToFileName1(String url)
	{
		String ftype = url.substring(url.lastIndexOf("/"));
		String fName = ftype.substring(ftype.lastIndexOf("."));
		// String fName1 = fName.substring(0, url.lastIndexOf("/") + 1);
		// return fName.replace(".", "") + ftype;
		return fName.replace(".", "");
	}

	/**
	 * 获取文件夹数据 这里用于获取相册最新的4张图片
	 * @param filePath
	 */
	public static ArrayList<HashMap<String, Object>> getFileDir(String filePath)
	{
		ArrayList<HashMap<String, Object>> vArrayList = new ArrayList<HashMap<String, Object>>();
		try
		{
			File f = new File(filePath);
			File[] files = f.listFiles();// 列出所有文件
			// 将所有文件存入list中
			if (files != null)
			{
				int count = files.length;// 文件个数
				for (int i = 0; i < count; i++)
				{
					File file = files[i];
					int typeIndex = file.getName().lastIndexOf(".");
					if (typeIndex != -1)
					{
						String fileType = file.getName()
								.substring(typeIndex + 1).toLowerCase();
						if (isImage(fileType))
						{
							HashMap<String, Object> vHashMap = new HashMap<String, Object>();
							vHashMap.put("photoName", file.getName());
							vHashMap.put("photoPath", file.getPath());
							vArrayList.add(vHashMap);
						}
					}

				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return vArrayList;
	}
	
	/**
	 * 根据后缀名判断是否是图片文件
	 * 
	 * @param type
	 * @return 是否是图片结果true or false
	 */
	public static boolean isImage(String type)
	{
		if (type != null
				&& (type.equals("jpg") || type.equals("gif")
						|| type.equals("png") || type.equals("jpeg")
						|| type.equals("bmp") || type.equals("wbmp")
						|| type.equals("ico") || type.equals("jpe")))
		{
			return true;
		}
		return false;
	}


	public static String getFileNameByPath(String pathandname) {

		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			return pathandname.substring(start + 1, end);
		} else {
			return null;
		}

	}
}

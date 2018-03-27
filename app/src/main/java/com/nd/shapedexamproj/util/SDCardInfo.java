package com.nd.shapedexamproj.util;

import java.io.File;

public class SDCardInfo {
	public long total;
	public long free;

	public static SDCardInfo getSDCardInfo() {

		String sDcString = android.os.Environment.getExternalStorageState();

		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {

			File pathFile = android.os.Environment
					.getExternalStorageDirectory();

			try {

				android.os.StatFs statfs = new android.os.StatFs(
						pathFile.getPath());

				// 获取SDCard上BLOCK总数

				long nTotalBlocks = statfs.getBlockCount();

				// 获取SDCard上每个block的SIZE

				long nBlocSize = statfs.getBlockSize();

				// 获取可供程序使用的Block的数量

				long nAvailaBlock = statfs.getAvailableBlocks();

				// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)

				long nFreeBlock = statfs.getFreeBlocks();

				SDCardInfo info = new SDCardInfo();

				// 计算SDCard 总容量大小MB

				info.total = nTotalBlocks * nBlocSize;

				// 计算 SDCard 剩余大小MB

				info.free = nAvailaBlock * nBlocSize;

				return info;

			} catch (IllegalArgumentException e) {

			}

		}

		return null;

	}
}

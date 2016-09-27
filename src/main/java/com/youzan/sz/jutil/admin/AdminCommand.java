package com.youzan.sz.jutil.admin;

import java.io.PrintWriter;

/**
 * 管理命令执行接口
 * @author meteor
 *
 */
public interface AdminCommand
{
	/**
	 * 执行命令
	 * @param argv	参数
	 * @param out	标准输出
	 */
	void execute(String[] argv, PrintWriter out);
}

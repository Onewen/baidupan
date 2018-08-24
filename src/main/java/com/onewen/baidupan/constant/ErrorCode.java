package com.onewen.baidupan.constant;

/**
 * 错误码
 * 
 * @author 梁光运
 * @date 2018年8月23日
 */
public final class ErrorCode {

	/**
	 * 登陆错误信息
	 * 
	 * @param erron 错误码
	 * @return
	 */
	public static String getLoginErrorMsg(int erron) {
		switch (erron) {
		case -1:
			return "系统错误,请您稍后再试,<a href=\"http://passport.baidu.com/v2/?ucenterfeedback#{urldata}#login\"  target=\"_blank\">帮助中心</a>";
		case 1:
			return "您输入的帐号格式不正确";
		case 2:
			return "用户名或密码有误，请重新输入或<a href=\"http://passport.baidu.com/?getpassindex#{urldata}\"  target=\"_blank\" >找回密码</a>";
		case 3:
			return "验证码不存在或已过期;请重新输入";
		case 4:
			return "帐号或密码错误，请重新输入或者<a href=\"http://passport.baidu.com/?getpassindex#{urldata}\"  target=\"_blank\" >找回密码</a>";
		case 5:
			return "";
		case 6:
			return "您输入的验证码有误";
		case 7:
			return "用户名或密码有误，请重新输入或<a href=\"http://passport.baidu.com/?getpassindex#{urldata}\"  target=\"_blank\" >找回密码</a>";
		case 16:
			return "您的帐号因安全问题已被限制登录;<a href=\"http://passport.baidu.com/v2/?ucenterfeedback#{urldata}#login\"  target=\"_blank\" >帮助中心</a>";
		case 257:
			return "请输入验证码";
		case 100027:
			return "百度正在进行系统升级，暂时不能提供服务，敬请谅解";
		case 120016:
			return "";
		case 18:
			return "";
		case 19:
			return "";
		case 20:
			return "";
		case 21:
			return "没有登录权限";
		case 22:
			return "";
		case 23:
			return "";
		case 24:
			return "百度正在进行系统升级，暂时不能提供服务，敬请谅解";
		case 400031:
			return "请在弹出的窗口操作;或重新登录";
		case 400032:
			return "";
		case 400034:
			return "";
		case 401007:
			return "您的手机号关联了其他帐号，请选择登录";
		case 120021:
			return "登录失败;请在弹出的窗口操作;或重新登录";
		case 500010:
			return "登录过于频繁;请24小时后再试";
		case 200010:
			return "验证码不存在或已过期";
		case 100005:
			return "系统错误;请您稍后再试";
		case 120019:
			return "请在弹出的窗口操作;或重新登录";
		case 110024:
			return "此帐号暂未激活;<a href=\"#{gotourl}\" >重发验证邮件</a>";
		case 100023:
			return "开启Cookie之后才能登录;<a href=\"http://passport.baidu.com/v2/?ucenterfeedback#{urldata}#login\"  target=\"_blank\" >如何开启</a>?";
		case 17:
			return "您的帐号已锁定;请<a href=\"http://passport.baidu.com/v2/?ucenterfeedback#login_10\" target=\"_blank\">解锁</a>后登录";
		case 400401:
			return "";
		case 400037:
			return "";
		case 50023:
			return "1个手机号30日内最多换绑3个账号";
		case 50024:
			return "注册过于频繁，请稍候再试";
		case 50025:
			return "注册过于频繁，请稍候再试；也可以通过上行短信的方式进行注册";
		case 50028:
			return "帐号或密码多次输错，请3个小时之后再试或<a href=\"http://passport.baidu.com/?getpassindex&getpassType=financePwdError#{urldata}\"  target=\"_blank\">找回密码</a>";
		case 50029:
			return "帐号或密码多次输错，请3个小时之后再试或<a href=\"http://passport.baidu.com/?getpassindex&getpassType=pwdError#{urldata}\"  target=\"_blank\">找回密码</a>";
		case 50030:
			return "抱歉，该手机号的申请次数已达当日上限，请更换手机号";
		case 50031:
			return "抱歉，该手机号的申请次数已达当月上限，请更换手机号";
		case 50032:
			return "抱歉，该手机号的申请次数已达本季度上限，请更换手机号";
		case 400413:
			return "";
		case 400414:
			return "";
		case 400415:
			return "帐号存在风险，为了您的帐号安全，请到百度钱包/理财/地图任一APP登录并完成验证，谢谢";
		case 400500:
			return "您登录的帐号已注销，请登录其他帐号或重新注册";
		case 72200:
			return "您的帐号因冻结暂时无法登录，请前往冻结时的手机APP，在登录页点击遇到问题进行解冻";
		case 96001:
			return "您的帐号因违反百度用户协议被限制登录";
		default:
			return "找不到错误信息";
		}
	}
	
	/**
	 * 校验验证码错误信息
	 * 
	 * @param erron 错误码
	 * @return
	 */
	public static String getCheckVerifycodeErrorMsg(int erron) {
		switch (erron) {
		case 500002:
			return "您输入的验证码有误";
		case 500018:
			return "验证码已失效，请重试";
		default:
			return "找不到错误信息";
		}
	}
}

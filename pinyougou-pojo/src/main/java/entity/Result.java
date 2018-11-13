package entity;

import java.io.Serializable;

/**
 * 返回结果
 * 
 * @author cy
 *
 */
public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean success;// 是否成功
	private String message;// 提示信息

	public Result(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

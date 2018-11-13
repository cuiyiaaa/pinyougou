package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 * @author cy
 *
 */
public class PageResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long total;//总记录数
	private List<T> rows;
	
	public PageResult(long total, List<T> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<T> getRows() {
		return rows;
	}
	public void setRows(List<T> rows) {
		this.rows = rows;
	}
}

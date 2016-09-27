package com.youzan.sz.jutil.util;

import java.io.Serializable;

/**
 * 3p，比Pair多一个值
 * @author phil
 *
 */
@SuppressWarnings("serial")
public final class Triple<F, S, T> implements Serializable
{
	private F first;
	private S second;
	private T third;
	
	/**
	 * 本来不想加缺省构造函数的，但是为了照顾hessian反射，加上吧
	 */
	public Triple()
	{
	    
	}
	
	public Triple(F f, S s, T t)
	{
		this.first = f;
		this.second = s;
		this.third = t;
	}
	
	/**
	 * 通过值创建值对
	 * @param f		第一个值
	 * @param s		第二个值
	 * @return		值对
	 */
	public static <F, S, T> Triple<F, S, T> makeTriple(F f, S s, T t)
	{
		return new Triple<F, S, T>(f, s, t);
	}
	
	private static <T> boolean eq(T o1, T o2)
	{
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		Triple<F, S, T> pr = (Triple<F, S, T>) o;
		if(pr == null)
			return false;
		return eq(first, pr.first)
				&& eq(second, pr.second)
				&& eq(third, pr.third);
	}
	
	private static int h(Object o)
	{
		return o == null ? 0 : o.hashCode();
	}
	
	public int hashCode()
	{
		int seed = h(first);
		seed ^= h(second) + 0x9e3779b9 + (seed << 6) + (seed >> 2);
		seed ^= h(third) + 0x9e3779b9 + (seed << 6) + (seed >> 2);
		return seed;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append(first)
			.append(", ")
			.append(second)
            .append(", ")
            .append(third)
			.append("}");
		return sb.toString();
	}

    public F getFirst()
    {
        return first;
    }

    public void setFirst(F first)
    {
        this.first = first;
    }

    public S getSecond()
    {
        return second;
    }

    public void setSecond(S second)
    {
        this.second = second;
    }

    public T getThird()
    {
        return third;
    }

    public void setThird(T third)
    {
        this.third = third;
    }
}

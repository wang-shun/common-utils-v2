package com.youzan.sz.jutil.j4log;


/**
 * 对于抛出对象的封装
 * @author kevinxu
 *
 */
final class ThrowableInfo
{
    private transient Throwable throwable;
    private String[] rep;
    
    public ThrowableInfo(Throwable throwable) 
    {
        this.throwable = throwable;
    }
    
    public Throwable getThrowable() 
    {
        return throwable;
    }
    
    public String[] getThrowableStrArr() 
    {
        if(throwable == null)
        {
            return null;
        }
        
        if(rep != null) 
        {
            return (String[]) rep.clone();
        }
        else 
        {
            ArrayWriter aw = new ArrayWriter();
            throwable.printStackTrace(aw);
            rep = aw.toStringArray();
            return rep;
        }
    }
    
    public String getThrowableStr()
    {
        String[] arr = getThrowableStrArr();
        if(arr == null)
        {
            return "";
        }
        
        StringBuilder strBuf = new StringBuilder();
        for(int i=0; i<arr.length; i++)
        {
            strBuf.append(arr[i]).append("\n");
        }
        return strBuf.toString();
    }
}

package com.youzan.sz.jutil.j4log;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * 专为产生抛出对象的堆栈的封装
 * @author kevinxu
 *
 */
final class ArrayWriter extends PrintWriter
{
    private ArrayList<String> alist;
    public ArrayWriter()
    {
        super(new NullWriter());
        alist = new ArrayList<String>();
    }
    
    public void print(Object o) 
    { 
        alist.add(o.toString());
    }
      
    public void print(char[] chars) 
    {
        alist.add(new String(chars));
    }
      
    public void print(String s) 
    {
        alist.add(s);
    }

    public void println(Object o) 
    {
        alist.add(o.toString());
    }
      
    public void println(char[] chars) 
    {
        alist.add(new String(chars));
    }
      
    public void println(String s) 
    {
        alist.add(s);
    }

    public void write(char[] chars) 
    {
        alist.add(new String(chars));
    }

    public void write(char[] chars, int off, int len) 
    {
        alist.add(new String(chars, off, len));
    }

    public void write(String s, int off, int len) 
    {
        alist.add(s.substring(off, off+len));
    }

    public void write(String s) 
    {
        alist.add(s);
    } 
    
    public String[] toStringArray() 
    {
        int len = alist.size();
        String[] result = new String[len];
        for(int i=0; i< len; i++)
        {
            result[i] = (String) alist.get(i);
        }
        return result;
    }
}


class NullWriter extends Writer 
{        
    public void close() 
    {
      // blank
    }

    public void flush() 
    {
      // blank
    }

    public void write(char[] cbuf, int off, int len) 
    {
      // blank
    }
}
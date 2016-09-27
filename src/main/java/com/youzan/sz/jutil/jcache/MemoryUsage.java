package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.util.ObjectWrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

//import com.qq.jutil.util.ObjectWrapper;

@Deprecated
@SuppressWarnings("unchecked")
public final class MemoryUsage
{
	/* Overhead for any object. */
	private static final int OBJECT_BASE = 8;

	/* Overhead for any array. */
	private static final int ARRAY_BASE = 12;

	/* The size of an object is padded with bytes to become multiple of OBJECT_ALIGN bytes. */
	private static final int OBJECT_ALIGN = 8;

	/* The size of an array is padded with bytes to become multiple of ARRAY_ALIGN bytes. */
	private static final int ARRAY_ALIGN = 8;

	/**
	 * Get the overhead for any object.
	 *
	 * @return the object overhead
	 */
	protected static int getObjectBase()
	{
		return OBJECT_BASE;
	}

	/**
	 * Get the overhead for any array.
	 *
	 * @return the array overhead
	 */
	protected static int getArrayBase()
	{
		return ARRAY_BASE;
	}

	/**
	 * Get the alignment for the size of an object.
	 *
	 * @return the object align
	 */
	protected static int getObjectAlign()
	{
		return OBJECT_ALIGN;
	}

	/**
	 * Get the alignment for the size of an array.
	 *
	 * @return the array align
	 */
	protected static int getArrayAlign()
	{
		return ARRAY_ALIGN;
	}

	/**
	 * Get the shallow memory usage for an object (not including embedded objects memory usage, but counting the memory
	 * consumed by the object to keep references to the embedded objects).
	 *
	 * @param obj the object to get the memory usage for
	 *
	 * @return the memory usage result
	 * @throws Exception 
	 *
	 * @throws MemoryUsageException wraps a reflection exception (generally security related)
	 */
	public static int getShallowMemoryUsage(Object obj)
			throws Exception
	{
		return analyze(obj, false);
	}

	/**
	 * Get the deep memory usage for an object (including embedded objects memory usage recursively).
	 *
	 * @param obj the object to get the memory usage for
	 * @param listener the memory usage listener
	 *
	 * @return the memory usage result
	 * @throws Exception 
	 *
	 * @throws MemoryUsageException wraps a reflection exception (generally security related)
	 */
	public static int getDeepMemoryUsage(Object obj) throws Exception
	{
		return analyze(obj, true);
	}

	/**
	 * Analyze memory usage for an object (with or without the embedded objects). See the component specification for
	 * details.
	 *
	 * @param obj the object to get the memory usage for
	 * @param goDeep are embedded objects considered
	 * @param listener the memory usage listener
	 *
	 * @return the memory usage result
	 *
	 * @throws Exception 
	 * @throws IllegalAccessException 
	 * @throws MemoryUsageException wraps a reflection exception (generally security related)
	 * @throws SecurityException if access to field information is denied
	 */
	protected static int analyze(Object obj, boolean goDeep)
			throws Exception
	{

		if (obj == null)
		{
			return 0;
		}

		int result = 0;
		Set<Object> visited = new HashSet<Object>();
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(obj);

		long size = 0;
		
		// BFS to go through all fields and embedded objects
		while (!queue.isEmpty())
		{
			Object currentObject = queue.removeFirst();
			if (currentObject.getClass().isArray())
			{
				// Overhead
				size = getArrayBase() + getArraySize(currentObject);

				// If we should process embedded objects  
				if (goDeep && currentObject instanceof Object[])
				{
					for (int i = 0; i < Array.getLength(currentObject); i++)
					{
						Object elem = Array.get(currentObject, i);

						// Don't include self references
						if (elem != obj)
						{
							addEmbeddedObject(elem, queue, visited);
						}
					}
				}

				// Align array size to multiple of ARRAY_ALIGN
				size = alignArray(size);
			}
			else
			{
				// Overhead
				size = getObjectBase();

				// Go through all the fields, including inherited ones
				for (Class cls = currentObject.getClass(); cls != null; cls = cls
						.getSuperclass())
				{
					Field[] fields = cls.getDeclaredFields();
					Field.setAccessible(fields, true);

					for (int i = 0; i < fields.length; i++)
					{
						// Skip static fields
						if (MemoryUsage.isStatic(fields[i]))
						{
							continue;
						}

						size += getFieldSize(fields[i].getType());

						// If we should process embedded objects
						if (goDeep && !fields[i].getType().isPrimitive())
						{
							Object value = fields[i].get(currentObject);

							// Don't include self references
							if (value != obj)
							{
								addEmbeddedObject(value, queue, visited);
							}
						}
					}
				}

				// Align object size to multiple of OBJECT_ALIGN
				size = alignObject(size);
			}

			// Cummulate this object's size to overall total
			result += size;
		}

		return result;
	}

	/**
	 * Get the size of a class field of the given class type. See the component specification for details.
	 *
	 * @param cls the type of the field
	 *
	 * @return the size in bytes
	 *
	 * @throws IllegalArgumentException if <code>cls</code> is null
	 */
	protected static int getFieldSize(Class cls) throws IllegalArgumentException
	{
		if (cls == null)
		{
			throw new IllegalArgumentException();
		}

		if (cls.isPrimitive())
		{
			if (cls == Double.TYPE || cls == Long.TYPE)
			{
				return 8;
			}

			if (cls == Integer.TYPE || cls == Float.TYPE)
			{
				return 4;
			}

			if (cls == Short.TYPE || cls == Character.TYPE)
			{
				return 2;
			}

			if (cls == Byte.TYPE || cls == Boolean.TYPE)
			{
				return 1;
			}
		}

		return 4;
	}

	/**
	 * Get the size of an array (shallow). See the component specification for details.
	 *
	 * @param arobj the array object
	 *
	 * @return the size in bytes
	 *
	 * @throws IllegalArgumentException if <code>arobj</code> is null or not an array (!isArray)
	 */
	protected static int getArraySize(Object arobj) throws IllegalArgumentException
	{
		if ((arobj == null) || !arobj.getClass().isArray())
		{
			throw new IllegalArgumentException();
		}

		Class array = arobj.getClass();
		int elementSize = getFieldSize(array.getComponentType());
		return elementSize * Array.getLength(arobj);
	}

	///////////////////////////////
	// Helpers for analyze method
	//

	/**
	 * Helper to make <code>size</code> a multiple of OBJECT_ALIGN
	 *
	 * @param size the calculated size
	 *
	 * @return <code>size</code> rounded up to multiple of OBJECT_ALIGN
	 */
	private static long alignObject(long size)
	{
		return (long) Math.ceil((double) size / getObjectAlign())
				* getObjectAlign();
	}

	/**
	 * Helper to make <code>size</code> a multiple of ARRAY_ALIGN
	 *
	 * @param size the calculated size
	 *
	 * @return <code>size</code> rounded up to multiple of ARRAY_ALIGN
	 */
	private static long alignArray(long size)
	{
		return (long) Math.ceil((double) size / getArrayAlign())
				* getArrayAlign();
	}

	/**
	 * Helper to add the embedded object to the visited siet and queue for further  analysis
	 *
	 * @param obj the embedded object
	 * @param listener listener to check if we should proceed to analyze embedded object
	 * @param queue the queue of objects to analyze
	 * @param visited a set of visited objects
	 */
	private static void addEmbeddedObject(Object obj, LinkedList<Object> queue, Set<Object> visited)
	{
		if (obj == null)
		{
			return;
		}

		ObjectWrapper embed = new ObjectWrapper(obj);

		if (!visited.contains(embed))
		{
			visited.add(embed);
			queue.addLast(obj);
		}
	}

	/**
	 * Helper to check if a field is static or not
	 *
	 * @param field the field to analyze
	 *
	 * @return <code>true</code> if the field is static, <code>false</code> otherwise
	 */
	protected static boolean isStatic(Field field)
	{
		int mod = field.getModifiers();
		return Modifier.isStatic(mod);
	}
}

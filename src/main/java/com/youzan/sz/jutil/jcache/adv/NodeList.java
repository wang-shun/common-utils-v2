package com.youzan.sz.jutil.jcache.adv;

final class NodeList<K, V>
{
	volatile Node<K, V>	head;
	volatile Node<K, V>	tail;
	
	public boolean isEmpty()
	{
		return head == null;
	}
	
	public void remove(Node<K, V> n)
	{
		if(n.next != null){
			n.next.prev = n.prev;
		}else{
			tail = n.prev;
			if(tail != null)
				tail.next = null;
		}
		if(n.prev != null){
			n.prev.next = n.next;
		}else{
			head = n.next;
			if(head != null)
				head.prev = null;
		}
	}
	
	public void add(Node<K, V> n)
	{
		if(head == null){
			tail = head = n;
		}else{
			n.next = head;
			head.prev = n;
			head = n;
			head.prev = null;
		}
	}
	
	public void moveToTop(Node<K, V> n)
	{
		if(head != tail){
			remove(n);
			add(n);
		}
	}
	
	public void clear()
	{
		head = tail = null;
	}
}
